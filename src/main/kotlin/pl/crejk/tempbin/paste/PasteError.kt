package pl.crejk.tempbin.paste

import io.ktor.http.*
import pl.crejk.tempbin.api.Response

enum class PasteError(
    val response: Response
) {

    NOT_FOUND("Paste not found", HttpStatusCode.NotFound),
    WRONG_PASSWORD("Wrong password", HttpStatusCode.Unauthorized),
    EXPIRED(NOT_FOUND.response),
    ;

    constructor(message: String, httpCode: HttpStatusCode): this(Response(message, httpCode))
}
