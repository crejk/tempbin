package pl.crejk.tempbin.paste

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import pl.crejk.tempbin.common.FakePasswordGenerator
import pl.crejk.tempbin.common.IncrementalIdGenerator
import pl.crejk.tempbin.common.testPasteService
import pl.crejk.tempbin.paste.api.CreatePasteRequest
import pl.crejk.tempbin.paste.api.PasteError
import pl.crejk.tempbin.paste.infrastructure.InMemoryPasteRepo

internal class PasteServiceTest : BehaviorSpec({
    Given("a service") {
        val service = testPasteService()

        When("created a paste") {
            val createdPaste = service.createPaste(CreatePasteRequest("test"))
            val id = createdPaste.id

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
