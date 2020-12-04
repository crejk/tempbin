package pl.crejk.tempbin.paste

internal interface PasteRepo {

    fun findPaste(id: String): Paste?

    fun savePaste(paste: Paste): Paste

    fun removePaste(id: String)
}
