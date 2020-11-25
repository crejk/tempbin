package pl.crejk.tempbin.paste

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import pl.crejk.tempbin.common.fp.Either
import pl.crejk.tempbin.common.fp.filterOrElse
import pl.crejk.tempbin.common.fp.toEither
import pl.crejk.tempbin.common.id.IdGenerator
import pl.crejk.tempbin.paste.api.CreatePasteRequest
import pl.crejk.tempbin.paste.api.PasteDto
import pl.crejk.tempbin.paste.api.PasteError
import java.util.concurrent.TimeUnit

class PasteService internal constructor(
    private val repo: PasteRepo,
    idGenerator: IdGenerator
) {

    private val creator = PasteCreator(idGenerator)
    private val cache = Caffeine.newBuilder()
        .expireAfterAccess(5, TimeUnit.MINUTES)
        .build<String, Paste>()

    suspend fun createPaste(request: CreatePasteRequest): PasteDto {
        val pasteWithPassword = this.creator.create(request)
        val paste = pasteWithPassword.first
        val password = pasteWithPassword.second

        return if (this.repo.savePaste(paste)) {
            this.cache.put(paste.id, paste)

            PasteDto(paste.id, password)
        } else {
            PasteDto.EMPTY
        }
    }

    suspend fun getPaste(id: String): Either<PasteError, Paste> =
        this.cache.computeIfAbsent(id, { this.repo.findPaste(id) })
            .toEither(PasteError.NOT_FOUND)
            .filterOrElse({ !it.isExpired() }, { PasteError.EXPIRED })

    suspend fun removePaste(id: String) {
        this.repo.removePaste(id)
        this.cache.invalidate(id)
    }

    private inline fun <K : Any, V> Cache<K, V>.computeIfAbsent(key: K, mappingFunction: (K) -> V?): V? =
        this.getIfPresent(key) ?: mappingFunction(key)?.also { this.put(key, it) }
}
