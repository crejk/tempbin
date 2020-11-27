package pl.crejk.tempbin.paste

import java.time.LocalDateTime

data class Paste(
    val id: String,
    val content: EncryptedContent,
    val creationTime: LocalDateTime,
    val expirationTime: LocalDateTime,
    val deleteAfterReading: Boolean = false,
    var visits: Int = 0
) {

    fun isExpired(): Boolean =
        this.expirationTime.isBefore(LocalDateTime.now())
}

data class EncryptedContent(val value: String, val salt: String) {

    companion object {

        internal val EMPTY = EncryptedContent("", "")
    }
}
