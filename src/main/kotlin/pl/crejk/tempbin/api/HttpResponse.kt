package pl.crejk.tempbin.api

import io.ktor.application.ApplicationCall
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond

data class HttpResponse(
    val message: Any,
    val httpCode: HttpStatusCode = HttpStatusCode.OK
)

@Suppress("NOTHING_TO_INLINE")
internal suspend inline fun ApplicationCall.respond(response: HttpResponse) =
    respond(response.httpCode, response.message)
