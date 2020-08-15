package pl.crejk.tempbin

import com.fasterxml.jackson.module.kotlin.KotlinModule
import io.ktor.application.install
import io.ktor.features.ContentNegotiation
import io.ktor.jackson.jackson
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Locations
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.util.InternalAPI
import io.ktor.util.encodeBase64
import org.apache.commons.codec.digest.DigestUtils
import pl.crejk.tempbin.paste.PasteRest
import pl.crejk.tempbin.paste.PasteService
import pl.crejk.tempbin.paste.repo.mem.InMemoryPasteRepo
import pl.crejk.tempbin.util.toBytes
import java.util.*

@ExperimentalStdlibApi
@InternalAPI
@KtorExperimentalLocationsAPI
fun main() {
    val pasteService = PasteService(InMemoryPasteRepo())

    val server = embeddedServer(Netty, port = 8080) {
        install(ContentNegotiation) {
            jackson {
                this.registerModule(KotlinModule())
            }
        }
        install(Locations)

        routing(PasteRest(pasteService).api())
    }

    server.start(wait = true)
}
