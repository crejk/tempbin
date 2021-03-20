package pl.crejk.tempbin.paste

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import io.vavr.control.Either
import io.vavr.control.Try
import io.vavr.kotlin.option
import java.util.concurrent.TimeUnit
import pl.crejk.tempbin.common.ValidationError
import pl.crejk.tempbin.common.id.IdGenerator
import pl.crejk.tempbin.common.id.RandomIdGenerator
import pl.crejk.tempbin.common.password.PasswordGenerator
import pl.crejk.tempbin.common.password.RandomPasswordGenerator
import pl.crejk.tempbin.paste.api.CreatePasteRequest
import pl.crejk.tempbin.paste.api.GetPasteError
import pl.crejk.tempbin.paste.api.PasteDto
import pl.crejk.tempbin.util.SecurityUtil

class PasteService internal constructor(
    private val repo: PasteRepo,
    idGenerator: IdGenerator = RandomIdGenerator(),
    passwordGenerator: PasswordGenerator = RandomPasswordGenerator(),
    maxContentLength: Int,
) {

    private val creator = PasteCreator(idGenerator, passwordGenerator, maxContentLength)
    private val cache = Caffeine.newBuilder()
        .expireAfterAccess(5, TimeUnit.MINUTES)
        .build<String, Paste>()

    fun createPaste(request: CreatePasteRequest): Either<ValidationError, PasteDto> =
        this.creator.create(request)
            .map { it.copy(paste = this.savePaste(it.paste)) }
            .map { PasteDto(it.paste.id, it.password) }

    private fun savePaste(paste: Paste): Paste {
        this.repo.savePaste(paste)
        this.cache.put(paste.id, paste)
        return paste
    }

    fun getPaste(id: String): Either<GetPasteError, Paste> =
        this.cache.computeIfAbsent(id, { this.repo.findPaste(id) })
            .option()
            .toEither(GetPasteError.NOT_FOUND)
            .peek { deletePasteIfNecessary(it) }
            .filterOrElse({ !it.isExpired() }, { GetPasteError.NOT_FOUND })

    private fun deletePasteIfNecessary(paste: Paste) {
        if (paste.deleteAfterReading || paste.isExpired()) {
            this.removePaste(paste.id)
        }
    }

    fun removePaste(id: String) {
        this.repo.removePaste(id)
        this.cache.invalidate(id)
    }

    fun getPasteContent(id: String, password: String): Either<GetPasteError, String> =
        this.getPaste(id).flatMap { decrypt(password, it.content).toEither(GetPasteError.WRONG_PASSWORD) }

    private fun decrypt(password: String, encryptedContent: EncryptedContent) = Try.of {
        SecurityUtil.prepareTextEncryptor(password, encryptedContent.salt).decrypt(encryptedContent.value)
    }
}

private inline fun <K : Any, V> Cache<K, V>.computeIfAbsent(key: K, mappingFunction: (K) -> V?): V? =
    this.getIfPresent(key) ?: mappingFunction(key)?.also { this.put(key, it) }
