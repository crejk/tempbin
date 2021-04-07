package pl.crejk.tempbin.paste.infrastructure

import pl.crejk.tempbin.paste.Paste
import pl.crejk.tempbin.paste.PasteRepo
import java.util.concurrent.ConcurrentHashMap

internal class InMemoryPasteRepo : PasteRepo {

    private val _pastes = ConcurrentHashMap<String, Paste>()

    override fun findPaste(id: String): Paste? =
        this._pastes[id]

    override fun savePaste(paste: Paste): Paste {
        this._pastes[paste.id] = paste
        return paste
    }

    override fun removePaste(id: String) {
        this._pastes.remove(id)
    }
}
