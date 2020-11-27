package pl.crejk.tempbin.paste.infrastructure

import io.ktor.application.*
import io.ktor.locations.*
import io.ktor.request.*
import io.ktor.routing.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import pl.crejk.tempbin.api.HttpResponse
import pl.crejk.tempbin.api.respond
import pl.crejk.tempbin.common.fp.*
import pl.crejk.tempbin.paste.EncryptedContent
import pl.crejk.tempbin.paste.PasteService
import pl.crejk.tempbin.paste.api.CreatePasteRequest
import pl.crejk.tempbin.paste.api.PasteError
import pl.crejk.tempbin.util.SecurityUtil

@KtorExperimentalLocationsAPI
@Location("/paste/{id}/{password}")
data class GetPasteRawRequest(
    val id: String,
    val password: String
)

@KtorExperimentalLocationsAPI
internal class PasteRest(
    private val service: PasteService,
    private val maxContentLength: Int
) {

    fun api(): Routing.() -> Unit = {
        get<GetPasteRawRequest> { request ->
            val response = service.getPaste(request.id)
                .filterOrElse(
                    { !it.deleteAfterReading || it.visits < 2 },
                    { PasteError.EXPIRED }
                )
                .leftPeekIf(
                    { error -> error == PasteError.EXPIRED },
                    { service.removePaste(request.id) })
                .rightPeek { it.visits++ }
                .flatMap { decrypt(request.password, it.content).toEither(PasteError.WRONG_PASSWORD) }
                .fold(
                    { error -> error.response },
                    { content -> HttpResponse(content) }
                )

            call.respond(response)
        }

        post("paste") {
            withContext(Dispatchers.IO) {
                val result = Try { call.receive<CreatePasteRequest>() }
                    .toEither(PasteError.BAD_REQUEST)
                    .filterOrElse(
                        { it.content.isNotEmpty() },
                        { PasteError.NO_CONTENT }
                    )
                    .filterOrElse(
                        { it.content.length < maxContentLength },
                        { PasteError.CONTENT_TOO_LARGE }
                    )
                    .map { service.createPaste(it) }
                    .fold(
                        { it.response },
                        { HttpResponse(it) }
                    )

                call.respond(result)
            }
        }
    }

    private fun decrypt(password: String, encryptedContent: EncryptedContent) = Try {
        SecurityUtil.prepareTextEncryptor(password, encryptedContent.salt).decrypt(encryptedContent.value)
    }
}
