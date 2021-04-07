package pl.crejk.tempbin.paste

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.kotest.assertions.ktor.shouldHaveContent
import io.kotest.assertions.ktor.shouldHaveStatus
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
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
import pl.crejk.tempbin.TimeProvider
import pl.crejk.tempbin.paste.api.CreatePasteRequest
import pl.crejk.tempbin.paste.api.PasteDto
import pl.crejk.tempbin.paste.infrastructure.PasteRest
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset

@KtorExperimentalLocationsAPI
internal class PasteRestTest : DescribeSpec({
    describe("rest server") {
        val mapper = jacksonObjectMapper()
        val engine = TestApplicationEngine()

        engine.start()

        engine.application.install(ContentNegotiation) {
            jackson {
                registerKotlinModule()
            }
        }

        engine.application.install(Locations)
        engine.application.routing(PasteRest(testPasteService(maxContentLength, timeProvider)).api())

        it("paste should not exist") {
            val response = engine.handleRequest(HttpMethod.Get, "/paste/1/pass").response

            response shouldHaveStatus HttpStatusCode.NotFound
        }

        it("should add paste") {
            val pasteDto = engine.handleRequest(HttpMethod.Post, "paste") {
                setBody(mapper.writeValueAsString(CreatePasteRequest("message")))
                addHeader("Content-Type", "application/json")
            }.response.content!!.let { mapper.readValue<PasteDto>(it) }

            pasteDto.id shouldBe "1"
        }

        it("should add second paste") {
            val pasteDto = engine.handleRequest(HttpMethod.Post, "paste") {
                this.setBody(mapper.writeValueAsString(CreatePasteRequest("message2")))
                this.addHeader("Content-Type", "application/json")
            }.response.content!!.let { mapper.readValue<PasteDto>(it) }

            pasteDto.id shouldBe "2"
        }

        it("first added paste's content should be returned") {
            val response = engine.handleRequest(HttpMethod.Get, "/paste/1/password").response

            response shouldHaveContent "message"
        }

        it("second added paste's content should be returned") {
            val response = engine.handleRequest(HttpMethod.Get, "/paste/2/password").response

            response shouldHaveContent "message2"
        }

        it("should fail when password is wrong") {
            val response = engine.handleRequest(HttpMethod.Get, "/paste/1/wrong_password").response

            response shouldHaveStatus HttpStatusCode.Unauthorized
        }

        it("should fail when content is too large") {
            val generatedPasteContent = generateString(4)

            val response = engine.handleRequest(HttpMethod.Post, "/paste") {
                this.setBody(mapper.writeValueAsString(CreatePasteRequest(generatedPasteContent)))
                this.addHeader("Content-Type", "application/json")
            }.response

            response shouldHaveStatus HttpStatusCode.BadRequest
        }

        it("should fail when adding paste with invalid duration") {
            val response = engine.handleRequest(HttpMethod.Post, "paste") {
                this.setBody("""{"content":"message", "duration": "invalid"}""")
                this.addHeader("Content-Type", "application/json")
            }.response

            response shouldHaveStatus HttpStatusCode.BadRequest
        }
    }
}) {

    companion object {

        private const val maxContentLength = (1 * 1024) / 2

        private val timeProvider: TimeProvider = suspend {
            Clock.fixed(Instant.parse("2014-12-22T10:15:30Z"), ZoneId.of(ZoneOffset.UTC.id)).instant()
        }

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
