package pl.crejk.tempbin.paste

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.ktor.locations.get
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.Routing
import io.ktor.routing.post
import kotlinx.coroutines.ObsoleteCoroutinesApi
import pl.crejk.tempbin.util.SecurityUtil
import java.util.*

@KtorExperimentalLocationsAPI
@Location("/paste/{id}/{password}")
data class GetPasteRequest(
    val id: String,
    val password: String
)

@ObsoleteCoroutinesApi
@KtorExperimentalLocationsAPI
class PasteRest(
    private val service: PasteService,
    maxContentLengthInKb: Int = 1 * 1000
) {

    private val maxContentLengthInMb = (maxContentLengthInKb / 2) * KB_LENGTH

    companion object {

        private const val KB_LENGTH = 1024
    }

    fun api(): Routing.() -> Unit = {
        get<GetPasteRequest> {
            val pasteId = try {
                UUID.fromString(it.id)
            } catch (e: Exception) {
                return@get call.respond(HttpStatusCode.BadRequest, "Invalid id")
            }

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
                    //return@get call.respond(ThymeleafContent("paste", mapOf("content" to content)))
                }

                service.removePaste(pasteId)
            }

            call.respond(HttpStatusCode.NotFound, "Paste doesn't exist")
        }

        post("paste") {
            val newPaste = call.receiveOrNull<PasteDTO>()

            if (newPaste == null || newPaste.content.isEmpty()) {
                return@post call.respond(HttpStatusCode.NoContent, "Missing content")
            }

            if ((newPaste.content.length / 2) > maxContentLengthInMb) {
                return@post call.respond(HttpStatusCode.PayloadTooLarge, "Content too large")
            }

            val result = service.createPaste(newPaste)

            call.respond(result)
        }
    }
}
