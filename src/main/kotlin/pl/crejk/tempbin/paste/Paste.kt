package pl.crejk.tempbin.paste

import pl.crejk.tempbin.TimeProvider
import pl.crejk.tempbin.common.SecurityUtil
import java.time.Instant

data class Paste(
    val id: String,
    val content: EncryptedContent,
    val creationTime: Instant,
    val expirationTime: Instant,
    val deleteAfterReading: Boolean = false
) {

    suspend fun isExpired(timeProvider: TimeProvider): Boolean =
        this.expirationTime.isBefore(timeProvider())
}

data class EncryptedContent(val value: String, val salt: String) {

    companion object {

        operator fun invoke(password: String, salt: String, content: String): EncryptedContent =
            EncryptedContent(
                SecurityUtil.prepareTextEncryptor(password, salt).encrypt(content),
                salt
            )
    }
}
