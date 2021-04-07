package pl.crejk.tempbin.common

import arrow.core.Either
import io.ktor.application.ApplicationCall
import io.ktor.request.receive

suspend inline fun <reified T : Any> ApplicationCall.receive(): Either<Throwable, T> =
    Either.catch { receive() }
