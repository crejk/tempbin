package pl.crejk.tempbin.paste.repo

import pl.crejk.tempbin.paste.Paste
import pl.crejk.tempbin.paste.PasteId

interface PasteRepo {

    suspend fun findPaste(id: PasteId): Paste?

    suspend fun savePaste(paste: Paste): Boolean

    suspend fun removePaste(id: PasteId): Boolean
}
