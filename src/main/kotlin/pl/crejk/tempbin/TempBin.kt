package pl.crejk.tempbin

import com.fasterxml.jackson.module.kotlin.KotlinModule
import io.ktor.application.install
import io.ktor.features.Compression
import io.ktor.features.ContentNegotiation
import io.ktor.jackson.jackson
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Locations
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import kotlinx.coroutines.ObsoleteCoroutinesApi
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

        routing(PasteRest(pasteService).api())
    }

    server.start(wait = true)
}
