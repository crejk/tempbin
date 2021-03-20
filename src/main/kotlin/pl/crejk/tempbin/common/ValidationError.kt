package pl.crejk.tempbin.common

import io.ktor.http.HttpStatusCode
import pl.crejk.tempbin.api.HttpResponse

data class ValidationError(
    private val message: String
) {

    fun toHttpResponse(): HttpResponse =
        HttpResponse(this.message, HttpStatusCode.BadRequest)
}
