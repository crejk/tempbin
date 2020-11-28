package pl.crejk.tempbin.paste.infrastructure

import pl.crejk.tempbin.paste.Paste
import pl.crejk.tempbin.paste.PasteRepo
import java.util.concurrent.ConcurrentHashMap

internal class InMemoryPasteRepo : PasteRepo {

    private val _pastes = ConcurrentHashMap<String, Paste>()

    override suspend fun findPaste(id: String): Paste? {
        val paste = this._pastes[id]

        return if (paste != null && paste.isExpired()) {
            this._pastes.remove(id)
            null
        } else paste
    }

    override suspend fun savePaste(paste: Paste): Paste {
        this._pastes[paste.id] = paste
        return paste
    }

    override suspend fun removePaste(id: String): Boolean =
        this._pastes.remove(id) != null
}
