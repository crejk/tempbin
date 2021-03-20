package pl.crejk.tempbin.paste

import io.vavr.control.Either
import io.vavr.kotlin.right
import pl.crejk.tempbin.common.ValidationError
import pl.crejk.tempbin.common.id.IdGenerator
import pl.crejk.tempbin.common.password.PasswordGenerator
import pl.crejk.tempbin.paste.api.CreatePasteRequest
import pl.crejk.tempbin.util.SecurityUtil
import java.time.LocalDateTime

typealias Validated<T> = Either<ValidationError, T>
typealias ValidatedRequest = Validated<CreatePasteRequest>

internal data class PasteWithPassword(
    val paste: Paste,
    val password: String
)

internal class PasteCreator(
    private val idGenerator: IdGenerator,
    private val passwordGenerator: PasswordGenerator,
    private val maxContentLength: Int
) {

    fun create(request: CreatePasteRequest): Validated<PasteWithPassword> =
        this.validateRequest(request)
            .map {
                val password = passwordGenerator.generate()
                val salt = SecurityUtil.generateSalt()
                val creationTime = LocalDateTime.now()

                PasteWithPassword(
                    Paste(
                        id = idGenerator.generate(),
                        content = EncryptedContent(password, salt, request.content),
                        creationTime = creationTime,
                        expirationTime = creationTime.plusMinutes(request.expiration.minutes),
                        deleteAfterReading = request.deleteAfterReading
                    ),
                    password
                )
            }

    private fun validateRequest(request: CreatePasteRequest): ValidatedRequest =
        right<ValidationError, CreatePasteRequest>(request)
            .filterOrElse({ it.content.isNotEmpty() }, { ValidationError("Content is empty.") })
            .filterOrElse({ it.content.length < maxContentLength }, { ValidationError("Content too large.") })
}
