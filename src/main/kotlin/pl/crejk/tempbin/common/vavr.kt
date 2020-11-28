package pl.crejk.tempbin.common

import io.ktor.application.*
import io.ktor.request.*
import io.vavr.control.Either
import io.vavr.control.Option
import io.vavr.control.Try
import io.vavr.kotlin.Try
import io.vavr.kotlin.none
import io.vavr.kotlin.some
import kotlin.reflect.KType

@Suppress("UNCHECKED_CAST")
inline fun <L, R, U> Either<L, R>.inlineMap(mapper: (R) -> U): Either<L, U> =
    if (this.isRight) {
        Either.right(mapper(this.get()))
    } else {
        this as Either<L, U>
    }

@Suppress("UNCHECKED_CAST")
inline fun <L, R, U> Either<L, R>.inlineFlatMap(mapper: (R) -> Either<L, U>): Either<L, U> =
    if(this.isRight) {
        mapper(this.get())
    } else {
        this as Either<L, U>
    }

inline fun <L, R> Either<L, R>.inlinePeek(action: (R) -> Unit): Either<L, R> {
    if (this.isRight) {
        action(this.get())
    }

    return this
}

suspend inline fun <reified T : Any> ApplicationCall.receiveOption(): Option<T> =
    try {
        some(receive(T::class))
    } catch (e: Exception) {
        none()
    }
