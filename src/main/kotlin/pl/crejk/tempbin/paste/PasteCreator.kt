package pl.crejk.tempbin.paste

import pl.crejk.tempbin.common.id.IdGenerator
import pl.crejk.tempbin.common.password.PasswordGenerator
import pl.crejk.tempbin.paste.api.CreatePasteRequest
import pl.crejk.tempbin.util.SecurityUtil
import java.time.LocalDateTime

typealias PastePassword = String

internal class PasteCreator(
    private val idGenerator: IdGenerator,
    private val passwordGenerator: PasswordGenerator
) {

    fun create(request: CreatePasteRequest): Pair<Paste, PastePassword> {
        val pasteId = this.idGenerator.generate()
        val password = this.passwordGenerator.generate()
        val salt = SecurityUtil.generateSalt()
        val encryptedContent = SecurityUtil.prepareTextEncryptor(password, salt).encrypt(request.content)
        val creationTime = LocalDateTime.now()
        val expirationTime = creationTime.plusNanos(request.expiration.nanos)

        return Paste(
            pasteId,
            EncryptedContent(encryptedContent, salt),
            creationTime,
            expirationTime,
            request.deleteAfterReading
        ) to password
    }
}
