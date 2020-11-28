package pl.crejk.tempbin.paste

internal interface PasteRepo {

    suspend fun findPaste(id: String): Paste?

    suspend fun savePaste(paste: Paste): Paste

    suspend fun removePaste(id: String): Boolean
}
