package pl.crejk.tempbin.common.fp

sealed class Either<out L, out R> {

    data class Left<L, R>(val left: L) : Either<L, R>()
    data class Right<L, R>(val right: R) : Either<L, R>()

    fun left(): L? = when (this) {
        is Left -> this.left
        is Right -> null
    }

    fun right(): R? = when (this) {
        is Left -> null
        is Right -> this.right
    }

    inline fun <U> map(mapper: (R) -> U): Either<L, U> = flatMap {
        Right(mapper(it))
    }

    inline fun <U> fold(leftMapper: (L) -> U, rightMapper: (R) -> U): U = when (this) {
        is Left -> leftMapper(this.left)
        is Right -> rightMapper(this.right)
    }
}

fun <L, R> right(right: R): Either<L, R> =
    Either.Right(right)

fun <L, R> left(left: L): Either<L, R> =
    Either.Left(left)

@Suppress("UNCHECKED_CAST")
inline fun <L, R, U> Either<L, R>.flatMap(mapper: (R) -> Either<L, U>): Either<L, U> = when(this) {
    is Either.Left -> this as Either<L, U>
    is Either.Right -> mapper(this.right)
}

fun <T, L> T?.toEither(left: L): Either<L, T> =
    if (this != null)
        Either.Right(this)
    else
        Either.Left(left)

inline fun <L, R> Either<L, R>.rightPeek(f: (R) -> Unit): Either<L, R> {
    if (this is Either.Right) {
        f(this.right)
    }

    return this
}

inline fun <L, R> Either<L, R>.leftPeekIf(peekIf: (L) -> Boolean, f: (L) -> Unit): Either<L, R> {
    if (this is Either.Left) {
        if (peekIf(this.left)) {
            f(this.left)
        }
    }
    return this
}

inline fun <L, R> Either<L, R>.filterOrElse(filter: (R) -> Boolean, orElse: () -> L): Either<L, R> = when(this) {
    is Either.Left -> this
    is Either.Right -> if (filter(this.right)) this else left(orElse())
}
