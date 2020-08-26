package pl.crejk.tempbin.paste

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import kotlinx.coroutines.*
import pl.crejk.tempbin.fp.*
import pl.crejk.tempbin.paste.repo.PasteRepo
import pl.crejk.tempbin.util.SecurityUtil
import java.time.LocalDateTime
import java.util.*
import java.util.concurrent.TimeUnit

@ObsoleteCoroutinesApi
class PasteService(
    private val repo: PasteRepo
) {

    private val executor = newFixedThreadPoolContext(4, "service-thread-pool")

    private val cache = Caffeine.newBuilder()
            .expireAfterAccess(5, TimeUnit.MINUTES)
            .build<PasteId, Paste>()

    suspend fun createPaste(newPaste: PasteDTO): PasteResult {
        val pasteId = UUID.randomUUID()
        val password = SecurityUtil.generatePassword()
        val salt = SecurityUtil.generateSalt()
        val encryptedContent = SecurityUtil.prepareTextEncryptor(password, salt).encrypt(newPaste.content)
        val creationTime = LocalDateTime.now()
        val expirationTime = creationTime.plusNanos(newPaste.expiration.nanos)

        val paste = Paste(
            pasteId,
            EncryptedContent(encryptedContent),
            salt,
            creationTime,
            expirationTime,
            newPaste.deleteAfterReading
        )

        return if (withContext(executor) { repo.savePaste(paste) }) {
            cache.put(paste.id, paste)

            PasteResult(paste.id.toString(), password)
        } else {
            PasteResult.EMPTY
        }
    }


    suspend fun getPaste(id: PasteId): Either<PasteError, Paste> =
        this.cache.getOrMap(id, { this.repo.findPaste(id) })
            .either(PasteError.NOT_FOUND)
            .filterOrElse({ !it.isExpired() }, { PasteError.EXPIRED })

    suspend fun getPasteContent(id: PasteId, password: String): Either<PasteError, String> =
        this.getPaste(id)
            .flatMap {
                this.decrypt(password, it.salt, it.content)
                    .either(PasteError.WRONG_PASSWORD)
            }

    private fun decrypt(password: String, salt: String, encryptedContent: EncryptedContent) = Try {
        SecurityUtil.prepareTextEncryptor(password, salt).decrypt(encryptedContent.value)
    }

    suspend fun removePaste(id: PasteId) =
        withContext(this.executor) {
            repo.removePaste(id)
        }.also {
            cache.invalidate(id)
        }
}

private inline fun <K : Any, V> Cache<K, V>.getOrMap(key: K, f: (K) -> V?) =
    this.getIfPresent(key) ?: f(key)?.also { this.put(key, it) }

data class PasteResult(
    val id: String,
    val password: String
) {

    companion object {

        val EMPTY = PasteResult("", "")
    }
}
