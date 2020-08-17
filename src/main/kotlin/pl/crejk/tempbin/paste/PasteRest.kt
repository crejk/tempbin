package pl.crejk.tempbin.paste

import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.ktor.locations.get
import io.ktor.request.receive
import io.ktor.request.receiveText
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.post
import kotlinx.coroutines.ObsoleteCoroutinesApi
import pl.crejk.tempbin.util.SecurityUtil

@KtorExperimentalLocationsAPI
@Location("paste/get/{id}/{password}")
data class GetPasteRequest(
    val id: String,
    val password: String
)

@ObsoleteCoroutinesApi
@KtorExperimentalLocationsAPI
class PasteRest(
    private val service: PasteService
) {

    fun api(): Routing.() -> Unit = {
        get<GetPasteRequest> {
            val pasteId = it.id
            val paste = service.getPaste(pasteId)

            if (paste != null) {
                if (!paste.isExpired()) {
                    val encryptedContent = paste.content.value

                    val content = try {
                        SecurityUtil.prepareTextEncryptor(it.password, paste.salt).decrypt(encryptedContent)
                    } catch (e: Exception) {
                        return@get call.respond(HttpStatusCode.Unauthorized, "Invalid password")
                    }

                    if (paste.deleteAfterReading) {
                        service.removePaste(pasteId)
                    }

                    return@get call.respond(content)
                }

                service.removePaste(pasteId)
            }

            call.respond(HttpStatusCode.NotFound, "Paste doesn't exist.")
        }

        post("paste") {
            val newPaste = call.receive<PasteDTO>()
            val result = service.createPaste(newPaste)

            call.respond(result)
        }
    }
}