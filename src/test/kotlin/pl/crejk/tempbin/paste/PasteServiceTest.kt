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

            Then("paste should be in cache and repo") {
                val paste = service.getPaste(id)

                paste.right()?.id shouldBe id
            }

            Then("paste should be removed from cache and repo") {
                service.removePaste(id)

                service.getPaste(id).left() shouldBe PasteError.NOT_FOUND
            }
        }
    }
})
