package pl.crejk.tempbin.paste.infrastructure

import java.util.concurrent.ConcurrentHashMap
import pl.crejk.tempbin.paste.Paste
import pl.crejk.tempbin.paste.PasteRepo

internal class InMemoryPasteRepo : PasteRepo {

    private val _pastes = ConcurrentHashMap<String, Paste>()

    override fun findPaste(id: String): Paste? {
        val paste = this._pastes[id]

        if (paste?.isExpired() == true) {
            this._pastes.remove(id)
            return null
        }

        return paste
    }

    override fun savePaste(paste: Paste): Paste {
        this._pastes[paste.id] = paste
        return paste
    }

    override fun removePaste(id: String) {
        this._pastes.remove(id)
    }
}
