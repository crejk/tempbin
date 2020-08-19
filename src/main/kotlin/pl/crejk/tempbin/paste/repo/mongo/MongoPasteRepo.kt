package pl.crejk.tempbin.paste.repo.mongo

import org.litote.kmongo.coroutine.CoroutineClient
import pl.crejk.tempbin.paste.Paste
import pl.crejk.tempbin.paste.PasteId
import pl.crejk.tempbin.paste.repo.PasteRepo

class MongoPasteRepo(
    client: CoroutineClient
): PasteRepo {

    private val database = client.getDatabase("tempbin")
    private val pastes = database.getCollection<Paste>("pastes")

    override suspend fun findPaste(id: PasteId): Paste? =
        this.pastes.findOneById(id.toString())

    override suspend fun savePaste(paste: Paste): Boolean =
        this.pastes.insertOne(paste).insertedId != null

    override suspend fun removePaste(id: PasteId): Boolean =
        this.pastes.deleteOneById(id.toString()).deletedCount == 1L
}
