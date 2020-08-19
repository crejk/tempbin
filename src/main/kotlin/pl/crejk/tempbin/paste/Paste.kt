package pl.crejk.tempbin.paste

import org.bson.codecs.pojo.annotations.BsonId
import java.time.LocalDateTime
import java.util.*

data class Paste(
    @BsonId
    val id: PasteId,
    val content: EncryptedContent,
    val salt: String,
    val creationTime: LocalDateTime,
    val expirationTime: LocalDateTime,
    val deleteAfterReading: Boolean = false
) {

    fun isExpired(): Boolean = this.expirationTime.isBefore(LocalDateTime.now())
}

typealias PasteId = UUID

inline class EncryptedContent(val value: String)
