package pl.crejk.tempbin

import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.jackson.*
import io.ktor.locations.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.netty.*
import io.ktor.thymeleaf.*
import io.ktor.util.*
import io.vavr.jackson.datatype.VavrModule
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver
import pl.crejk.tempbin.paste.PasteRepo
import pl.crejk.tempbin.paste.infrastructure.PasteRest
import pl.crejk.tempbin.paste.PasteService
import pl.crejk.tempbin.paste.infrastructure.FlatPasteRepo
import pl.crejk.tempbin.paste.infrastructure.InMemoryPasteRepo

fun main(args: Array<String>) =
    EngineMain.main(args)

@KtorExperimentalLocationsAPI
@KtorExperimentalAPI
fun Application.module() {
    install(ContentNegotiation) {
        jackson {
            this.registerKotlinModule()
            this.registerModule(VavrModule())
        }
    }

    val maxContentSizeInMb = environment.config.property("tempbin.content.maxSizeInMb").getString().toInt()
    val maxContentSize = (maxContentSizeInMb * 1024 * 1024)
    // char is 2 bytes
    val maxContentLength = maxContentSize / 2

    val repoProperty = environment.config.property("tempbin.repo").getString()

    val repo: PasteRepo = if (repoProperty == "inMemory")
        InMemoryPasteRepo()
    else
        FlatPasteRepo()

    val pasteService = PasteService(repo, maxContentLength = maxContentLength)

    install(StatusPages) {
        exception<Throwable> { cause ->
            cause.printStackTrace()
            call.respond(HttpStatusCode.InternalServerError)
        }
    }

    install(Compression) {
        default()
    }

    install(Locations)

    install(Thymeleaf) {
        setTemplateResolver(ClassLoaderTemplateResolver().apply {
            prefix = "templates/thymeleaf/"
            suffix = ".html"
            characterEncoding = "utf-8"
        })
    }

    routing(PasteRest(pasteService).api())

    routing {
        get("/") {
            call.respond(ThymeleafContent("index", mapOf()))
        }
    }
}
