package pl.crejk.tempbin.fp

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

    fun <L> toEither(left: L): Either<L, T> = when(this) {
        is Success -> right(this.value)
        is Failure -> left(left)
    }
}
