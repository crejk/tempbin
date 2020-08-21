package pl.crejk.tempbin

import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.mongodb.reactivestreams.client.MongoClients
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
import org.litote.kmongo.coroutine.coroutine
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver
import pl.crejk.tempbin.paste.PasteRest
import pl.crejk.tempbin.paste.PasteService
import pl.crejk.tempbin.paste.repo.PasteRepo
import pl.crejk.tempbin.paste.repo.mem.InMemoryPasteRepo
import pl.crejk.tempbin.paste.repo.mongo.MongoPasteRepo

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
    val mongoUriProperty = environment.config.property("tempbin.mongodb.uri").getString()

    val repo: PasteRepo = if (repoProperty == "inMemory")
        InMemoryPasteRepo()
    else
        MongoPasteRepo(MongoClients.create(mongoUriProperty).coroutine)

    val pasteService = PasteService(repo)

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
