package pl.crejk.tempbin.paste

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.jackson.*
import io.ktor.locations.*
import io.ktor.routing.*
import io.ktor.server.testing.*
import kotlinx.coroutines.ObsoleteCoroutinesApi
import pl.crejk.tempbin.paste.repo.mem.InMemoryPasteRepo
import pl.crejk.tempbin.util.SecurityUtil
import java.util.*

@KtorExperimentalLocationsAPI
@ObsoleteCoroutinesApi
class PasteRestTest : DescribeSpec({
    val mapper = jacksonObjectMapper()

    describe("rest server") {
        val repo = InMemoryPasteRepo()
        val maxContentLength = (1 * 1024) / 2
        val service = PasteService(repo)
        val engine = TestApplicationEngine()

        engine.start()

        engine.application.install(ContentNegotiation) {
            jackson {
                registerKotlinModule()
            }
        }
        engine.application.install(Locations)
        engine.application.routing(PasteRest(service, maxContentLength).api())

        it("paste status code should be 'NotFound'") {
            val response = engine.handleRequest(HttpMethod.Get, "/paste/${UUID.randomUUID()}/pass").response

            response.status() shouldBe HttpStatusCode.NotFound
        }

        it("should add paste") {
            val response = engine.handleRequest(HttpMethod.Post, "paste") {
                this.setBody(mapper.writeValueAsString(PasteDTO("message")))
                this.addHeader("Content-Type", "application/json")
            }.response

            val result = mapper.readValue<PasteResult>(response.content!!)

            result shouldNotBe PasteResult.EMPTY
            result.id shouldNotBe UUID.randomUUID()
            result.password shouldNotBe SecurityUtil.generatePassword()
        }

        it("added paste should be returned") {
            val addedPaste = repo.pastes.single()
            val response = engine.handleRequest(HttpMethod.Get, "/paste/${addedPaste.id}/invalidpassword").response

            response.status() shouldBe HttpStatusCode.Unauthorized
        }

        it("too large content status should be returned") {
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
            val size = sizeInKb * 1024
            val sb = StringBuilder(size)

            for (i in 0 until size) {
                sb.append('a')
            }

            return sb.toString()
        }
    }
}
