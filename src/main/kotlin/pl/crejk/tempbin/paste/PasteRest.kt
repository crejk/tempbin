package pl.crejk.tempbin.paste

import io.ktor.application.*
import io.ktor.locations.*
import io.ktor.request.*
import io.ktor.routing.*
import kotlinx.coroutines.ObsoleteCoroutinesApi
import pl.crejk.tempbin.api.HttpResponse
import pl.crejk.tempbin.api.respond
import pl.crejk.tempbin.fp.Try
import pl.crejk.tempbin.fp.filterOrElse
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
    private val maxContentLength: Int
) {

    fun api(): Routing.() -> Unit = {
        get<GetPasteRawRequest> {
            val id = UUID.fromString(it.id)

            val response = service.getPasteContent(id, it.password)
                .leftPeekIf(
                    { error -> error == PasteError.EXPIRED },
                    { service.removePaste(id) })
                .fold(
                    { error -> error.response },
                    { content -> HttpResponse(content) }
                )

            call.respond(response)
        }

        post("paste") {
            val result = Try { call.receive<PasteDTO>() }
                .filter { it.content.isNotEmpty() }
                .either(PasteError.NO_CONTENT)
                .filterOrElse({ it.content.length < maxContentLength }, { PasteError.CONTENT_TOO_LARGE })
                .map { service.createPaste(it) }
                .fold(
                    { it.response },
                    { HttpResponse(it) }
                )

            call.respond(result)
        }
    }
}
