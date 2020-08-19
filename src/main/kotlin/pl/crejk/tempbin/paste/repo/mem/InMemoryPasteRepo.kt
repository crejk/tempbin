package pl.crejk.tempbin.paste.repo.mem

import pl.crejk.tempbin.paste.Paste
import pl.crejk.tempbin.paste.PasteId
import pl.crejk.tempbin.paste.repo.PasteRepo
import java.util.concurrent.ConcurrentHashMap

class InMemoryPasteRepo : PasteRepo {

    private val _pastes = ConcurrentHashMap<PasteId, Paste>()
    internal val pastes get(): Collection<Paste> = this._pastes.values

    override suspend fun findPaste(id: PasteId): Paste? =
        this._pastes[id]

    override suspend fun savePaste(paste: Paste): Boolean {
        this._pastes[paste.id] = paste
        return true
    }

    override suspend fun removePaste(id: PasteId): Boolean =
        this._pastes.remove(id) != null
}
