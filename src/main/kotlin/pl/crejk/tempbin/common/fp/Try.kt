package pl.crejk.tempbin.common.fp

sealed class Try<out T> {

    data class Success<T>(val value: T) : Try<T>()
    data class Failure(val throwable: Throwable) : Try<Nothing>()

    companion object {

        inline operator fun <T> invoke(f: () -> T): Try<T> = try {
            Success(f())
        } catch (throwable: Throwable) {
            Failure(throwable)
        }
    }

    inline fun filter(predicate: (T) -> Boolean): Try<T> = when (this) {
        is Success -> if (predicate(this.value))
            this
        else
            Failure(NoSuchElementException("Predicate does not hold for ${this.value}"))
        is Failure -> this
    }

    fun <L> toEither(left: L): Either<L, T> = when (this) {
        is Success -> right(this.value)
        is Failure -> left(left)
    }
}
