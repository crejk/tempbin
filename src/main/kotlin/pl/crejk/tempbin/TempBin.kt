package pl.crejk.tempbin

import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.jackson.*
import io.ktor.locations.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.thymeleaf.*
import kotlinx.coroutines.ObsoleteCoroutinesApi
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver
import pl.crejk.tempbin.paste.PasteRest
import pl.crejk.tempbin.paste.PasteService
import pl.crejk.tempbin.paste.repo.mem.InMemoryPasteRepo

@ObsoleteCoroutinesApi
@KtorExperimentalLocationsAPI
fun main() {
    val maxContentSizeInMb = 10
    val maxContentSize = (maxContentSizeInMb * 1024 * 1024)
    // char is 2 bytes
    val maxContentLength = maxContentSize / 2
    val pasteService = PasteService(InMemoryPasteRepo())

    val server = embeddedServer(Netty, port = 8080) {
        install(ContentNegotiation) {
            jackson {
                this.registerKotlinModule()
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

        routing(PasteRest(pasteService, maxContentLength).api())

        routing {
            get("/") {
                call.respond(ThymeleafContent("index", mapOf()))
            }
        }
    }

    server.start(wait = true)
}
