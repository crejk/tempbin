package pl.crejk.tempbin.paste.infrastructure

import pl.crejk.tempbin.paste.Paste
import pl.crejk.tempbin.paste.PasteRepo

internal class FlatPasteRepo : PasteRepo {

    override suspend fun findPaste(id: String): Paste? {
        TODO("Not yet implemented")
    }

    override suspend fun savePaste(paste: Paste): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun removePaste(id: String): Boolean {
        TODO("Not yet implemented")
    }
}
