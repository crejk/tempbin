package pl.crejk.tempbin.paste

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.ObsoleteCoroutinesApi
import pl.crejk.tempbin.common.id.RandomIdGenerator
import pl.crejk.tempbin.paste.api.CreatePasteRequest
import pl.crejk.tempbin.paste.api.PasteError
import pl.crejk.tempbin.paste.infrastructure.InMemoryPasteRepo

@ObsoleteCoroutinesApi
class PasteServiceTest : BehaviorSpec({
    Given("a service") {
        val service = PasteService(InMemoryPasteRepo(), RandomIdGenerator())

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
