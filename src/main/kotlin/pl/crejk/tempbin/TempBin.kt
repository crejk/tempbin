package pl.crejk.tempbin

import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.jackson.*
import io.ktor.locations.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.netty.*
import io.ktor.thymeleaf.*
import io.ktor.util.*
import kotlinx.coroutines.ObsoleteCoroutinesApi
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver
import pl.crejk.tempbin.common.id.RandomIdGenerator
import pl.crejk.tempbin.paste.PasteRepo
import pl.crejk.tempbin.paste.PasteRest
import pl.crejk.tempbin.paste.PasteService
import pl.crejk.tempbin.paste.infrastructure.FlatPasteRepo
import pl.crejk.tempbin.paste.infrastructure.InMemoryPasteRepo

fun main(args: Array<String>) =
    EngineMain.main(args)

@KtorExperimentalAPI
@ObsoleteCoroutinesApi
@KtorExperimentalLocationsAPI
fun Application.module() {
    install(ContentNegotiation) {
        jackson {
            this.registerKotlinModule()
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

    val pasteService = PasteService(repo, RandomIdGenerator())

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

    routing(PasteRest(pasteService, maxContentLength).api())

    routing {
        get("/") {
            call.respond(ThymeleafContent("index", mapOf()))
        }
    }
}
