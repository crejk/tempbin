package pl.crejk.tempbin.paste

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.ObsoleteCoroutinesApi
import pl.crejk.tempbin.paste.repo.mem.InMemoryPasteRepo
import java.util.*

@ObsoleteCoroutinesApi
class PasteServiceTest : BehaviorSpec({
    Given("a service") {
        val service = PasteService(InMemoryPasteRepo())

        When("created a paste") {
            val createdPaste = service.createPaste(PasteDTO("test"))
            val id = UUID.fromString(createdPaste.id)

            Then("paste should be in repo") {
                val paste = service.getPaste(id)

                paste?.id shouldBe id
            }

            Then("paste should be removed from repo") {
                service.removePaste(id)

                service.getPaste(id) shouldBe null
            }
        }
    }
})
