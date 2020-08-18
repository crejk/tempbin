package pl.crejk.tempbin

import com.fasterxml.jackson.module.kotlin.KotlinModule
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.Compression
import io.ktor.features.ContentNegotiation
import io.ktor.jackson.jackson
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Locations
import io.ktor.response.respond
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.thymeleaf.Thymeleaf
import io.ktor.thymeleaf.ThymeleafContent
import kotlinx.coroutines.ObsoleteCoroutinesApi
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver
import pl.crejk.tempbin.paste.PasteRest
import pl.crejk.tempbin.paste.PasteService
import pl.crejk.tempbin.paste.repo.mem.InMemoryPasteRepo

@ObsoleteCoroutinesApi
@KtorExperimentalLocationsAPI
fun main() {
    val pasteService = PasteService(InMemoryPasteRepo())

    val server = embeddedServer(Netty, port = 8080) {
        install(ContentNegotiation) {
            jackson {
                this.registerModule(KotlinModule())
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

    server.start(wait = true)
}
