package pl.crejk.tempbin.paste

import java.time.LocalDateTime
import pl.crejk.tempbin.util.SecurityUtil

data class Paste(
    val id: String,
    val content: EncryptedContent,
    val creationTime: LocalDateTime,
    val expirationTime: LocalDateTime,
    val deleteAfterReading: Boolean = false
) {

    fun isExpired(): Boolean =
        this.expirationTime.isBefore(LocalDateTime.now())
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
