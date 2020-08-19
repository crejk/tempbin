package pl.crejk.tempbin.paste

import com.github.benmanes.caffeine.cache.Caffeine
import kotlinx.coroutines.*
import pl.crejk.tempbin.paste.repo.PasteRepo
import pl.crejk.tempbin.util.SecurityUtil
import pl.crejk.tempbin.util.SuspendingCache
import java.time.LocalDateTime
import java.util.*
import java.util.concurrent.TimeUnit

@ObsoleteCoroutinesApi
class PasteService(
    private val repo: PasteRepo
) {

    private val compute = newFixedThreadPoolContext(4, "service-thread-pool")

    private val cache = SuspendingCache<PasteId, Paste>(
        Caffeine.newBuilder()
            .executor(this.compute.executor)
            .maximumSize(10000)
            .expireAfterAccess(5, TimeUnit.MINUTES)
            .buildAsync<PasteId, Paste>()
    ) {
        this.repo.findPaste(it)
    }

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

        return withContext(this.compute) {
            if (repo.savePaste(paste))
                PasteResult(paste.id.toString(), password)
            else
                PasteResult.EMPTY
        }
    }

    suspend fun getPaste(id: PasteId): Paste? =
        this.cache.get(id)

    suspend fun removePaste(id: PasteId) = coroutineScope {
        async(compute) {
            repo.removePaste(id)
        }.invokeOnCompletion {
            cache.invalidate(id)
        }
    }
}

data class PasteResult(
    val id: String,
    val password: String
) {

    companion object {

        val EMPTY = PasteResult("", "")
    }
}
