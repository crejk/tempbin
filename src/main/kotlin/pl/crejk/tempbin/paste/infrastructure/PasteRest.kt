package pl.crejk.tempbin.paste.infrastructure

import io.ktor.application.*
import io.ktor.locations.*
import io.ktor.routing.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import pl.crejk.tempbin.api.HttpResponse
import pl.crejk.tempbin.api.respond
import pl.crejk.tempbin.common.ValidationError
import pl.crejk.tempbin.common.receiveOption
import pl.crejk.tempbin.paste.PasteService
import pl.crejk.tempbin.paste.api.CreatePasteRequest

@KtorExperimentalLocationsAPI
@Location("/paste/{id}/{password}")
data class GetPasteRawRequest(
    val id: String,
    val password: String
)

@KtorExperimentalLocationsAPI
internal class PasteRest(
    private val service: PasteService
) {

    fun api(): Routing.() -> Unit = {
        get<GetPasteRawRequest> { request ->
            val response = service.getPasteContent(request.id, request.password)
                .fold(
                    { it.response },
                    { HttpResponse(it) }
                )

            call.respond(response)
        }

        post("paste") {
            withContext(Dispatchers.IO) {
                val response = call.receiveOption<CreatePasteRequest>()
                    .toEither { ValidationError("Bad request") }
                    .flatMap { service.createPaste(it) }
                    .fold(
                        { it.toHttpResponse() },
                        { HttpResponse(it) }
                    )

                call.respond(response)
            }
        }
    }
}
