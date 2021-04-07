package pl.crejk.tempbin.paste

import arrow.core.Either
import arrow.core.Option
import arrow.core.flatMap
import arrow.core.none
import arrow.core.orElse
import arrow.core.some
import arrow.core.toOption
import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import pl.crejk.tempbin.TimeProvider
import pl.crejk.tempbin.common.SecurityUtil
import pl.crejk.tempbin.paste.api.CreatePasteRequest
import pl.crejk.tempbin.paste.api.PasteDto
import pl.crejk.tempbin.paste.api.PasteError
import java.util.concurrent.TimeUnit

class PasteService internal constructor(
    private val repo: PasteRepo,
    private val creator: PasteCreator,
    private val timeProvider: TimeProvider,
    private val cache: Cache<String, Paste> = Caffeine.newBuilder()
        .expireAfterAccess(5, TimeUnit.MINUTES)
        .build()
) {

    suspend fun createPaste(request: CreatePasteRequest): Either<PasteError, PasteDto> =
        creator.create(request)
            .map { it.copy(paste = this.savePaste(it.paste)) }
            .map { PasteDto(it.paste.id, it.password) }

    suspend fun getPaste(id: String): Either<PasteError, Paste> =
        this.cache.computeIfAbsent(id, { this.repo.findPaste(id) })
            .toOption()
            .flatMap { deletePasteIfNecessary(it) }
            .toEither { PasteError.NotFound }

    suspend fun getPasteContent(id: String, password: String): Either<PasteError, String> =
        getPaste(id).flatMap { decrypt(password, it.content) }

    fun removePaste(id: String): Option<Paste> {
        this.repo.removePaste(id)
        this.cache.invalidate(id)
        return none()
    }

    private suspend fun deletePasteIfNecessary(paste: Paste): Option<Paste> = when {
        paste.isExpired(timeProvider) -> removePaste(paste.id)
        paste.deleteAfterReading -> removePaste(paste.id).orElse { paste.some() }
        else -> paste.some()
    }

    private fun savePaste(paste: Paste): Paste {
        repo.savePaste(paste)
        cache.put(paste.id, paste)
        return paste
    }

    private fun decrypt(password: String, encryptedContent: EncryptedContent): Either<PasteError, String> =
        Either.catch {
            SecurityUtil.prepareTextEncryptor(password, encryptedContent.salt).decrypt(encryptedContent.value)
        }.mapLeft { PasteError.Unauthorized }
}

private inline fun <K : Any, V> Cache<K, V>.computeIfAbsent(key: K, mappingFunction: (K) -> V?): V? =
    getIfPresent(key) ?: mappingFunction(key)?.also { this.put(key, it) }
