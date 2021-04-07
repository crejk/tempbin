package pl.crejk.tempbin.paste

import io.kotest.assertions.arrow.either.shouldBeLeft
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import pl.crejk.tempbin.paste.api.CreatePasteRequest
import pl.crejk.tempbin.paste.api.PasteError
import java.time.Instant

internal class PasteServiceTest : BehaviorSpec({
    Given("a service") {
        val service = testPasteService(timeProvider = suspend { Instant.now() })

        When("created a paste") {
            service.createPaste(CreatePasteRequest("test"))

            Then("paste should be in cache and repo") {
                val paste = service.getPaste("1")

                paste.orNull()?.id shouldBe "1"
            }

            Then("paste should be removed from cache and repo") {
                service.removePaste("1")

                service.getPaste("1") shouldBeLeft PasteError.NotFound
            }
        }
    }
})
