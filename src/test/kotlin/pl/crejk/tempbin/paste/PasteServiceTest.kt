package pl.crejk.tempbin.paste

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import pl.crejk.tempbin.common.testPasteService
import pl.crejk.tempbin.paste.api.CreatePasteRequest
import pl.crejk.tempbin.paste.api.GetPasteError

internal class PasteServiceTest : BehaviorSpec({
    Given("a service") {
        val service = testPasteService()

        When("created a paste") {
            service.createPaste(CreatePasteRequest("test"))

            Then("paste should be in cache and repo") {
                val paste = service.getPaste("1")

                paste.get()?.id shouldBe "1"
            }

            Then("paste should be removed from cache and repo") {
                service.removePaste("1")

                service.getPaste("1").left shouldBe GetPasteError.NOT_FOUND
            }
        }
    }
})
