package pl.crejk.tempbin.paste.api

import io.ktor.http.HttpStatusCode
import pl.crejk.tempbin.api.HttpResponse

enum class GetPasteError(
    val response: HttpResponse
) {

    NOT_FOUND("Paste not found", HttpStatusCode.NotFound),
    WRONG_PASSWORD("Wrong password", HttpStatusCode.Unauthorized),
    ;

    constructor(message: String, httpCode: HttpStatusCode) : this(HttpResponse(message, httpCode))
}
