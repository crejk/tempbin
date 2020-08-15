package pl.crejk.tempbin.paste

import com.github.benmanes.caffeine.cache.CacheLoader
import com.github.benmanes.caffeine.cache.Caffeine
import pl.crejk.tempbin.paste.repo.PasteRepo
import pl.crejk.tempbin.util.SecurityUtil
import java.time.LocalDateTime
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class PasteService(
    private val repo: PasteRepo
) {

    private val executor = Executors.newFixedThreadPool(4)

    private val cache = Caffeine.newBuilder()
        .executor(this.executor)
        .maximumSize(10000)
        .expireAfterAccess(5, TimeUnit.MINUTES)
        .buildAsync(PasteLoader(this.repo))

    fun createPaste(pasteDTO: PasteDTO): PasteResult {
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

        val paste = Paste(pasteId, EncryptedContent(encryptedContent), salt, creationTime, expirationTime)

        val promise = CompletableFuture<Paste?>()

        this.executor.execute {
            promise.complete(this.repo.savePaste(paste))
        }

        this.cache.put(paste.id, promise)

        return PasteResult(pasteId, password)
    }

    fun getPaste(id: PasteId): CompletableFuture<Paste?> =
        this.cache.get(id)

    fun removePaste(id: PasteId) {
        val promise = CompletableFuture<Paste?>()

        this.executor.execute {
            val paste = this.repo.removePaste(id)

            promise.complete(paste)
        }

        promise.thenApply {
            this.cache.synchronous().invalidate(id)
        }
    }
}

class PasteLoader(
    private val repo: PasteRepo
): CacheLoader<String, Paste> {

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
