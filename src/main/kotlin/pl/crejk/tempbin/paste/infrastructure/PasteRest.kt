package pl.crejk.tempbin.paste.infrastructure

import arrow.core.flatMap
import io.ktor.application.call
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.ktor.locations.get
import io.ktor.routing.Routing
import io.ktor.routing.post
import pl.crejk.tempbin.api.HttpResponse
import pl.crejk.tempbin.api.respond
import pl.crejk.tempbin.common.receive
import pl.crejk.tempbin.paste.PasteService
import pl.crejk.tempbin.paste.api.CreatePasteRequest
import pl.crejk.tempbin.paste.api.PasteError

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
                    { error -> error.toHttpResponse() },
                    { HttpResponse(it) }
                )

            call.respond(response)
        }

        post("paste") {
            val response = call.receive<CreatePasteRequest>()
                .mapLeft { PasteError.BadRequest.CannotTransformContent }
                .flatMap { service.createPaste(it) }
                .fold(
                    { error -> error.toHttpResponse() },
                    { HttpResponse(it) }
                )

            call.respond(response)
        }
    }
}
