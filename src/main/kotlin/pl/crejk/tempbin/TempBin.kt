package pl.crejk.tempbin

import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.Compression
import io.ktor.features.ContentNegotiation
import io.ktor.features.StatusPages
import io.ktor.http.HttpStatusCode
import io.ktor.jackson.jackson
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Locations
import io.ktor.response.respond
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.server.netty.EngineMain
import io.ktor.thymeleaf.Thymeleaf
import io.ktor.thymeleaf.ThymeleafContent
import io.vavr.jackson.datatype.VavrModule
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver
import pl.crejk.tempbin.paste.PasteRepo
import pl.crejk.tempbin.paste.PasteService
import pl.crejk.tempbin.paste.infrastructure.FlatPasteRepo
import pl.crejk.tempbin.paste.infrastructure.InMemoryPasteRepo
import pl.crejk.tempbin.paste.infrastructure.PasteRest

fun main(args: Array<String>) =
    EngineMain.main(args)

@KtorExperimentalLocationsAPI
fun Application.module() {
    install(ContentNegotiation) {
        jackson {
            registerKotlinModule()
            registerModule(VavrModule())
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
        setTemplateResolver(
            ClassLoaderTemplateResolver().apply {
                prefix = "templates/thymeleaf/"
                suffix = ".html"
                characterEncoding = "utf-8"
            }
        )
    }

    routing(PasteRest(pasteService).api())

    routing {
        get("/") {
            call.respond(ThymeleafContent("index", mapOf()))
        }
    }
}
