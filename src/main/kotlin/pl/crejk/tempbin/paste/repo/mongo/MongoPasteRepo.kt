package pl.crejk.tempbin.paste.repo.mongo

import pl.crejk.tempbin.paste.Paste
import pl.crejk.tempbin.paste.PasteId
import pl.crejk.tempbin.paste.repo.PasteRepo

class MongoPasteRepo : PasteRepo {

    override fun findPaste(id: String): Paste? {
        TODO("Not yet implemented")
    }

    override fun savePaste(paste: Paste): Paste? {
        TODO("Not yet implemented")
    }

    override fun removePaste(id: PasteId): Paste? {
        TODO("Not yet implemented")
    }
}
