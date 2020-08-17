package pl.crejk.tempbin.paste

import com.fasterxml.jackson.databind.ObjectMapper
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

@KtorExperimentalLocationsAPI
@ObsoleteCoroutinesApi
class PasteRestTest : DescribeSpec({
    val mapper = jacksonObjectMapper()

    describe("rest server") {
        val repo = InMemoryPasteRepo()
        val service = PasteService(repo)
        val engine = TestApplicationEngine()

        engine.start()

        engine.application.install(ContentNegotiation) {
            jackson {
                registerKotlinModule()
            }
        }
        engine.application.install(Locations)
        engine.application.routing(PasteRest(service).api())

        it("paste status code should be 'NotFound'") {
            val response = engine.handleRequest(HttpMethod.Get, "/paste/get/testid/testpassword").response

            response.status() shouldBe HttpStatusCode.NotFound
        }

        it("should add paste") {
            val response = engine.handleRequest(HttpMethod.Post, "/paste") {
                this.setBody(mapper.writeValueAsString(Test("message")))
                this.addHeader("Content-Type", "application/json")
            }.response.content!!

            val result = mapper.readValue<PasteResult>(response)

            result shouldNotBe PasteResult.EMPTY
            result.id shouldNotBe SecurityUtil.generateId()
            result.password shouldNotBe SecurityUtil.generatePassword()
        }

        it("added paste should be returned") {
            val addedPaste = repo.pastes.single()
            val response = engine.handleRequest(HttpMethod.Get, "/paste/get/${addedPaste.id}/invalidpassword").response

            response.status() shouldBe HttpStatusCode.Unauthorized
        }
    }
})
