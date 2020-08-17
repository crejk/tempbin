package pl.crejk.tempbin.paste

import com.github.benmanes.caffeine.cache.CacheLoader
import com.github.benmanes.caffeine.cache.Caffeine
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.future.await
import kotlinx.coroutines.newFixedThreadPoolContext
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
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

    suspend fun createPaste(pasteDTO: PasteDTO): PasteResult {
        val content = pasteDTO.content

        if (content.isEmpty()) {
            return PasteResult.EMPTY
        }

        val pasteId = SecurityUtil.generateId()
        val password = SecurityUtil.generatePassword()
        val salt = SecurityUtil.generateSalt()
        val encryptedContent = SecurityUtil.prepareTextEncryptor(password, salt).encrypt(content)
        val creationTime = LocalDateTime.now()
        val expirationTime = creationTime.plusNanos(pasteDTO.expiration.nanos)

        val paste = Paste(pasteId, EncryptedContent(encryptedContent), salt, creationTime, expirationTime, pasteDTO.deleteAfterReading)

        return withContext(this.compute) {
            repo.savePaste(paste)?.let {
                PasteResult(it.id, password)
            } ?: PasteResult.EMPTY
        }
    }

    suspend fun getPaste(id: PasteId): Paste? =
        this.cache.get(id).await()

    suspend fun removePaste(id: PasteId) {
        withContext(this.compute) {
            repo.removePaste(id)
        }

        this.cache.synchronous().invalidate(id)
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
