package pl.crejk.tempbin.paste

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlinx.coroutines.ObsoleteCoroutinesApi
import pl.crejk.tempbin.paste.repo.mem.InMemoryPasteRepo
import pl.crejk.tempbin.util.SecurityUtil
import java.util.concurrent.TimeUnit

@ObsoleteCoroutinesApi
class PasteServiceTest : BehaviorSpec({
    Given("a service") {
        val service = PasteService(InMemoryPasteRepo())

        When("created a paste") {
            val createdPaste = service.createPaste(PasteDTO("test"))

            Then("paste should be in repo") {
                val paste = service.getPaste(createdPaste.id)

                paste?.id shouldBe createdPaste.id
            }

            Then("paste should be removed from repo") {
                service.removePaste(createdPaste.id)

                service.getPaste(createdPaste.id) shouldBe null
            }
        }
    }
})
