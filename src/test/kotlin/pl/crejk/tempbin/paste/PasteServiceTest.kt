package pl.crejk.tempbin.paste

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import pl.crejk.tempbin.paste.repo.mem.InMemoryPasteRepo
import pl.crejk.tempbin.util.SecurityUtil
import java.util.concurrent.TimeUnit

class PasteServiceTest : BehaviorSpec({
    val service = PasteService(InMemoryPasteRepo())

    val createdPaste = service.createPaste(PasteDTO("test"))
    val paste = service.getPaste(createdPaste.id).get(3, TimeUnit.SECONDS)!!

    paste.id shouldBe createdPaste.id
})
