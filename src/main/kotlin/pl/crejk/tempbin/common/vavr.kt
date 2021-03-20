package pl.crejk.tempbin.common

import io.ktor.application.ApplicationCall
import io.ktor.request.receive
import io.vavr.control.Option
import io.vavr.kotlin.none
import io.vavr.kotlin.some

suspend inline fun <reified T : Any> ApplicationCall.receiveOption(): Option<T> =
    try {
        some(receive(T::class))
    } catch (e: Exception) {
        none()
    }
