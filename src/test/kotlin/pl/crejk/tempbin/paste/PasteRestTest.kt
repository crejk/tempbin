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
import kotlinx.coroutines.ObsoleteCoroutinesApi
import pl.crejk.tempbin.common.id.RandomIdGenerator
import pl.crejk.tempbin.paste.api.CreatePasteRequest
import pl.crejk.tempbin.paste.api.PasteDto
import pl.crejk.tempbin.paste.infrastructure.InMemoryPasteRepo
import java.util.*

@KtorExperimentalLocationsAPI
@ObsoleteCoroutinesApi
internal class PasteRestTest : DescribeSpec({
    val mapper = jacksonObjectMapper()

    describe("rest server") {
        val repo = InMemoryPasteRepo()
        val idGenerator = RandomIdGenerator()
        val service = PasteService(repo, idGenerator)
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
            val response = engine.handleRequest(HttpMethod.Get, "/paste/${UUID.randomUUID()}/pass").response

            response.status() shouldBe HttpStatusCode.NotFound
        }

        describe("adding paste") {
            val pasteDto = engine.handleRequest(HttpMethod.Post, "paste") {
                this.setBody(mapper.writeValueAsString(CreatePasteRequest("message")))
                this.addHeader("Content-Type", "application/json")
            }.response.content!!.let { mapper.readValue<PasteDto>(it) }

            it("added paste should be returned") {
                val response = engine.handleRequest(HttpMethod.Get, "/paste/${pasteDto.id}/${pasteDto.password}").response

                response.content shouldBe "message"
            }

            it("should fail because password is wrong") {
                val response = engine.handleRequest(HttpMethod.Get, "/paste/${pasteDto.id}/wrong_password").response

                response.status() shouldBe HttpStatusCode.Unauthorized
            }
        }

        it("too large content status should be returned") {
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
