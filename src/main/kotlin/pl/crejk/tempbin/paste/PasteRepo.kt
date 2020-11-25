package pl.crejk.tempbin.paste

internal interface PasteRepo {

    suspend fun findPaste(id: String): Paste?

    suspend fun savePaste(paste: Paste): Boolean

    suspend fun removePaste(id: String): Boolean
}
