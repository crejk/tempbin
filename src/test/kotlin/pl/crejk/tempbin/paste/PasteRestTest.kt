package pl.crejk.tempbin.paste

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.jackson.*
import io.ktor.locations.*
import io.ktor.routing.*
import io.ktor.server.testing.*
import pl.crejk.tempbin.common.FakePasswordGenerator
import pl.crejk.tempbin.common.IncrementalIdGenerator
import pl.crejk.tempbin.paste.api.CreatePasteRequest
import pl.crejk.tempbin.paste.api.PasteDto
import pl.crejk.tempbin.paste.infrastructure.InMemoryPasteRepo

@KtorExperimentalLocationsAPI
internal class PasteRestTest : DescribeSpec({
    describe("rest server") {
        val mapper = jacksonObjectMapper()
        val repo = InMemoryPasteRepo()
        val idGenerator = IncrementalIdGenerator()
        val passwordGenerator = FakePasswordGenerator()
        val service = PasteService(repo, idGenerator, passwordGenerator)
        val maxContentLength = (1 * 1024) / 2
        val engine = TestApplicationEngine()

        engine.start()

        engine.application.install(ContentNegotiation) {
            jackson {
                registerKotlinModule()
            }
        }
        engine.application.install(Locations)
        engine.application.routing(PasteRest(service, maxContentLength).api())

        it("should not found") {
            val response = engine.handleRequest(HttpMethod.Get, "/paste/1/pass").response

            response.status() shouldBe HttpStatusCode.NotFound
        }

        it("should add paste") {
            val pasteDto = engine.handleRequest(HttpMethod.Post, "paste") {
                this.setBody(mapper.writeValueAsString(CreatePasteRequest("message")))
                this.addHeader("Content-Type", "application/json")
            }.response.content!!.let { mapper.readValue<PasteDto>(it) }

            pasteDto.id shouldBe "1"
        }

        it("should add second paste") {
            val pasteDto = engine.handleRequest(HttpMethod.Post, "paste") {
                this.setBody(mapper.writeValueAsString(CreatePasteRequest("message")))
                this.addHeader("Content-Type", "application/json")
            }.response.content!!.let { mapper.readValue<PasteDto>(it) }

            pasteDto.id shouldBe "2"
        }

        it("first added paste's content should be returned") {
            val response = engine.handleRequest(HttpMethod.Get, "/paste/1/password").response

            response.content shouldBe "message"
        }

        it("second added paste's content should be returned") {
            val response = engine.handleRequest(HttpMethod.Get, "/paste/2/password").response

            response.content shouldBe "message"
        }

        it("should fail when password is wrong") {
            val response = engine.handleRequest(HttpMethod.Get, "/paste/1/wrong_password").response

            response.status() shouldBe HttpStatusCode.Unauthorized
        }

        it("should fail when content is too large") {
            val generatedPasteContent = generateString(4)

            val response = engine.handleRequest(HttpMethod.Post, "/paste") {
                this.setBody(mapper.writeValueAsString(CreatePasteRequest(generatedPasteContent)))
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
