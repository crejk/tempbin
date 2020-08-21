package pl.crejk.tempbin.api

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*

data class HttpResponse(
    val message: Any,
    val httpCode: HttpStatusCode = HttpStatusCode.OK
)

@Suppress("NOTHING_TO_INLINE")
internal suspend inline fun ApplicationCall.respond(response: HttpResponse) =
    this.respond(response.httpCode, response.message)
