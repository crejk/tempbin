package pl.crejk.tempbin.paste

import java.time.LocalDateTime

data class Paste(
    val id: PasteId,
    val content: EncryptedContent,
    val salt: String,
    val creationTime: LocalDateTime,
    val expirationTime: LocalDateTime,
    val deleteAfterReading: Boolean = false
) {

    fun isExpired(): Boolean = this.expirationTime.isBefore(LocalDateTime.now())
}

typealias PasteId = String

inline class EncryptedContent(val value: String)
