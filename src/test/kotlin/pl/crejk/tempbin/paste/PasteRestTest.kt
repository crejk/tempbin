package pl.crejk.tempbin.paste

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.ktor.application.install
import io.ktor.features.ContentNegotiation
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.jackson.jackson
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Locations
import io.ktor.routing.routing
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import kotlinx.coroutines.ObsoleteCoroutinesApi
import pl.crejk.tempbin.paste.repo.mem.InMemoryPasteRepo
import pl.crejk.tempbin.util.SecurityUtil
import java.lang.StringBuilder

@KtorExperimentalLocationsAPI
@ObsoleteCoroutinesApi
class PasteRestTest : DescribeSpec({
    val mapper = jacksonObjectMapper()

    describe("rest server") {
        val repo = InMemoryPasteRepo()
        val service = PasteService(repo)
        val maxContentLengthInKb = 2
        val engine = TestApplicationEngine()

        engine.start()

        engine.application.install(ContentNegotiation) {
            jackson {
                registerKotlinModule()
            }
        }
        engine.application.install(Locations)
        engine.application.routing(PasteRest(service, maxContentLengthInKb).api())

        it("paste status code should be 'NotFound'") {
            val response = engine.handleRequest(HttpMethod.Get, "/paste/get/testid/testpassword").response

            response.status() shouldBe HttpStatusCode.NotFound
        }

        it("should add paste") {
            val response = engine.handleRequest(HttpMethod.Post, "/paste") {
                this.setBody(mapper.writeValueAsString(PasteDTO("message")))
                this.addHeader("Content-Type", "application/json")
            }.response

            val result = mapper.readValue<PasteResult>(response.content!!)

            result shouldNotBe PasteResult.EMPTY
            result.id shouldNotBe SecurityUtil.generateId()
            result.password shouldNotBe SecurityUtil.generatePassword()
        }

        it("added paste should be returned") {
            val addedPaste = repo.pastes.single()
            val response = engine.handleRequest(HttpMethod.Get, "/paste/get/${addedPaste.id}/invalidpassword").response

            response.status() shouldBe HttpStatusCode.Unauthorized
        }

        it("too large content") {
            val message = generateString(4)
            val response = engine.handleRequest(HttpMethod.Post, "/paste") {
                this.setBody(mapper.writeValueAsString(PasteDTO(message)))
                this.addHeader("Content-Type", "application/json")
            }.response

            response.status() shouldBe HttpStatusCode.PayloadTooLarge
        }
    }
}) {

    companion object {

        private fun generateString(sizeInKb: Int): String {
            val size = (sizeInKb / 2) * 1024
            val sb = StringBuilder(size)

            for (i in 0 until size) {
                sb.append('a')
            }

            return sb.toString()
        }
    }
}
