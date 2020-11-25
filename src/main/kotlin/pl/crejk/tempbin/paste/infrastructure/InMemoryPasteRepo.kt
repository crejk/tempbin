package pl.crejk.tempbin.paste.infrastructure

import pl.crejk.tempbin.paste.Paste
import pl.crejk.tempbin.paste.PasteRepo
import java.util.concurrent.ConcurrentHashMap

internal class InMemoryPasteRepo : PasteRepo {

    private val _pastes = ConcurrentHashMap<String, Paste>()

    override suspend fun findPaste(id: String): Paste? =
        this._pastes[id]

    override suspend fun savePaste(paste: Paste): Boolean {
        this._pastes[paste.id] = paste
        return true
    }

    override suspend fun removePaste(id: String): Boolean =
        this._pastes.remove(id) != null
}
