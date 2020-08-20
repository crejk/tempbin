package pl.crejk.tempbin.paste

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.locations.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.routing.post
import kotlinx.coroutines.ObsoleteCoroutinesApi
import pl.crejk.tempbin.api.Response
import pl.crejk.tempbin.api.respond
import pl.crejk.tempbin.fp.leftPeekIf
import java.util.*

@KtorExperimentalLocationsAPI
@Location("/paste/{id}/{password}")
data class GetPasteRawRequest(
    val id: String,
    val password: String
)

@ObsoleteCoroutinesApi
@KtorExperimentalLocationsAPI
class PasteRest(
    private val service: PasteService,
    maxContentLengthInKb: Int = 1000
) {

    private val maxContentLengthInMb = maxContentLengthInKb * KB_LENGTH

    companion object {

        private const val KB_LENGTH = 1024
    }

    fun api(): Routing.() -> Unit = {
        get<GetPasteRawRequest> {
            val id = UUID.fromString(it.id)

            val response = service.getPasteContent(id, it.password)
                .leftPeekIf(
                    { error -> error == PasteError.EXPIRED },
                    { service.removePaste(id) })
                .fold(
                    { error -> error.response },
                    { content -> Response(content) }
                )

            call.respond(response)
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
