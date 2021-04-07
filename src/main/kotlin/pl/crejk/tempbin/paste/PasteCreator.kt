package pl.crejk.tempbin.paste

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import pl.crejk.tempbin.TimeProvider
import pl.crejk.tempbin.common.SecurityUtil
import pl.crejk.tempbin.common.id.IdGenerator
import pl.crejk.tempbin.common.password.PasswordGenerator
import pl.crejk.tempbin.paste.api.CreatePasteRequest
import pl.crejk.tempbin.paste.api.PasteError

typealias Validated<T> = Either<PasteError.BadRequest, T>
typealias ValidatedRequest = Validated<CreatePasteRequest>

internal data class PasteWithPassword(
    val paste: Paste,
    val password: String
)

internal class PasteCreator(
    private val idGenerator: IdGenerator,
    private val passwordGenerator: PasswordGenerator,
    private val maxContentLength: Int,
    private val timeProvider: TimeProvider
) {

    suspend fun create(request: CreatePasteRequest): Validated<PasteWithPassword> =
        this.validateRequest(request).map { fromValidatedRequest(request) }

    private suspend fun fromValidatedRequest(request: CreatePasteRequest): PasteWithPassword {
        val password = passwordGenerator.generate()
        val salt = SecurityUtil.generateSalt()
        val creationTime = timeProvider()

        return PasteWithPassword(
            Paste(
                id = idGenerator.generate(),
                content = EncryptedContent(password, salt, request.content),
                creationTime = creationTime,
                expirationTime = creationTime.plusMillis(request.expiration.toMillis),
                deleteAfterReading = request.deleteAfterReading
            ),
            password
        )
    }

    private fun validateRequest(request: CreatePasteRequest): ValidatedRequest = when {
        request.content.isEmpty() -> PasteError.BadRequest.EmptyContent.left()
        request.content.length > maxContentLength -> PasteError.BadRequest.ContentTooLarge.left()
        else -> request.right()
    }
}
