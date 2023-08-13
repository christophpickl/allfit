package allfit.api

import allfit.TestDates
import allfit.api.models.categoriesJsonRoot
import allfit.service.kotlinxSerializer
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.HttpHeaders
import io.ktor.http.headersOf
import io.ktor.utils.io.ByteReadChannel
import kotlinx.serialization.encodeToString

class OnefitHttpClientTest : DescribeSpec() {

    private val categoriesJsonRoot = Arb.categoriesJsonRoot().next()

    init {
        describe("getCategories") {
            it("Success") {
                val client = buildClient {
                    respond(
                        content = toContent(categoriesJsonRoot),
                        headers = headersOf(HttpHeaders.ContentType, "application/json")
                    )
                }

                val categories = client.getCategories()

                categories shouldBe categoriesJsonRoot
            }
        }
        // TODO write more tests for onefit http client
    }

    private inline fun <reified T> toContent(jsonResponse: T) =
        ByteReadChannel(kotlinxSerializer.encodeToString(jsonResponse))

    private fun buildClient(handler: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData) =
        OnefitHttpClient(
            "any token", TestDates.clock, NoOpJsonLogFileManager, buildHttpClient("any token", MockEngine(handler))
        )
}
