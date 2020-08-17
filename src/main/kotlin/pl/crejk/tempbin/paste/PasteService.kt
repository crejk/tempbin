package pl.crejk.tempbin.paste

import com.github.benmanes.caffeine.cache.CacheLoader
import com.github.benmanes.caffeine.cache.Caffeine
import kotlinx.coroutines.*
import kotlinx.coroutines.future.await
import pl.crejk.tempbin.paste.repo.PasteRepo
import pl.crejk.tempbin.util.SecurityUtil
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

@ObsoleteCoroutinesApi
class PasteService(
    private val repo: PasteRepo
) {

    private val compute = newFixedThreadPoolContext(4, "service-thread-pool")

    private val cache = Caffeine.newBuilder()
        .executor(this.compute.executor)
        .maximumSize(10000)
        .expireAfterAccess(5, TimeUnit.MINUTES)
        .buildAsync(PasteLoader(this.repo))

    suspend fun createPaste(newPaste: PasteDTO): PasteResult {
        val pasteId = SecurityUtil.generateId()
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

        return withContext(this.compute) {
            repo.savePaste(paste)?.let {
                PasteResult(it.id, password)
            } ?: PasteResult.EMPTY
        }
    }

    suspend fun getPaste(id: PasteId): Paste? =
        this.cache.get(id).await()

    suspend fun removePaste(id: PasteId) = coroutineScope {
        async(compute) {
            repo.removePaste(id)
        }.invokeOnCompletion {
            cache.synchronous().invalidate(id)
        }
    }
}

class PasteLoader(
    private val repo: PasteRepo
): CacheLoader<PasteId, Paste> {

    override fun load(key: String): Paste? =
        this.repo.findPaste(key)
}

data class PasteResult(
    val id: String,
    val password: String
) {

    companion object {

        val EMPTY = PasteResult("", "")
    }
}
