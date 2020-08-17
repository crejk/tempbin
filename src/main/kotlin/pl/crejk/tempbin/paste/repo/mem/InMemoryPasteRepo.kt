package pl.crejk.tempbin.paste.repo.mem

import pl.crejk.tempbin.paste.Paste
import pl.crejk.tempbin.paste.PasteId
import pl.crejk.tempbin.paste.repo.PasteRepo
import java.util.concurrent.ConcurrentHashMap

class InMemoryPasteRepo : PasteRepo {

    private val _pastes = ConcurrentHashMap<String, Paste>()
    internal val pastes get(): Collection<Paste> = this._pastes.values

    override fun findPaste(id: String): Paste? =
        this._pastes[id]

    override fun savePaste(paste: Paste): Paste? {
        this._pastes[paste.id] = paste
        return paste
    }

    override fun removePaste(id: PasteId): Paste? =
        this._pastes.remove(id)
}
