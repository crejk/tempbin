package pl.crejk.tempbin.paste.api

import io.ktor.http.*
import pl.crejk.tempbin.api.HttpResponse

enum class PasteError(
    val response: HttpResponse
) {

    NOT_FOUND("Paste not found", HttpStatusCode.NotFound),
    WRONG_PASSWORD("Wrong password", HttpStatusCode.Unauthorized),
    EXPIRED(NOT_FOUND.response),
    NO_CONTENT("Missing content", HttpStatusCode.NoContent),
    CONTENT_TOO_LARGE("Content too large", HttpStatusCode.PayloadTooLarge)
    ;

    constructor(message: String, httpCode: HttpStatusCode): this(HttpResponse(message, httpCode))
}
