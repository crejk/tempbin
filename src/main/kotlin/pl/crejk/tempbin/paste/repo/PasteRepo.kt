package pl.crejk.tempbin.paste.repo

import pl.crejk.tempbin.paste.Paste
import pl.crejk.tempbin.paste.PasteId

interface PasteRepo {

    fun findPaste(id: PasteId): Paste?

    fun savePaste(paste: Paste): Paste?

    fun removePaste(id: PasteId): Paste?
}
