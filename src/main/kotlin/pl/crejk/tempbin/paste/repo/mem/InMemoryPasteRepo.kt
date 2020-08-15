package pl.crejk.tempbin.paste.repo.mem

import pl.crejk.tempbin.paste.Paste
import pl.crejk.tempbin.paste.PasteId
import pl.crejk.tempbin.paste.repo.PasteRepo
import java.util.concurrent.ConcurrentHashMap

class InMemoryPasteRepo : PasteRepo {

    private val pastes = ConcurrentHashMap<String, Paste>()

    override fun findPaste(id: String): Paste? =
        this.pastes[id]

    override fun savePaste(paste: Paste): Paste? =
        this.pastes.put(paste.id, paste)

    override fun removePaste(id: PasteId): Paste? =
        this.pastes.remove(id)
}
