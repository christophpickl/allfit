package allfit.api

import allfit.api.models.AuthJson
import allfit.api.models.AuthResponseJson
import allfit.api.models.CategoriesJson
import allfit.api.models.PartnersJson
import allfit.readApiResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.client.statement.request
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import mu.KotlinLogging.logger

interface OnefitClient {

    suspend fun getCategories(): CategoriesJson
    suspend fun getPartners(): PartnersJson

    companion object {

        private val log = logger {}
        private val authClient = buildClient(null)

        suspend fun authenticate(credentials: Credentials): OnefitClient {
            val response = authClient.post("") {
                header("Content-Type", "application/json")
                setBody(
                    AuthJson(
                        email = credentials.email,
                        password = credentials.password,
                    )
                )
            }
            response.requireOk()
            log.info { "Login success." }
            return RealOnefitClient(response.body<AuthResponseJson>().access_token)
        }
    }
}

object ClassPathOnefitClient : OnefitClient {
    override suspend fun getCategories() =
        readApiResponse<CategoriesJson>("partners_categories.json")

    override suspend fun getPartners() =
        readApiResponse<PartnersJson>("partners.json")
}

private fun buildClient(authToken: String?) = HttpClient(CIO) {
    install(ContentNegotiation) {
        json(Json {
            isLenient = true
            allowSpecialFloatingPointValues = true
            allowStructuredMapKeys = true
            prettyPrint = true
            useArrayPolymorphism = false
            ignoreUnknownKeys = true
        })
    }
    expectSuccess = false
    defaultRequest {
        url(if (authToken == null) "https://one.fit/api/auth" else "https://api.one.fit/v2/en-nl/")
        header("Accept", "application/vnd.onefit.v2.1+json")
        header("X-ONEFIT-CLIENT", "website/29.14.2")
        if (authToken != null) {
            bearerAuth(authToken)
        }
    }
}

class RealOnefitClient(
    authToken: String
) : OnefitClient {

    private val log = logger {}
    private val client = buildClient(authToken)

    private suspend inline fun <reified T> simpleGet(
        path: String,
        requestModifier: HttpRequestBuilder.() -> Unit = {}
    ): T {
        val response = client.get(path) {
            requestModifier()
        }
        log.debug { "${response.status.value} GET ${response.request.url}" }
        response.requireOk()
        return response.body()
    }

    override suspend fun getCategories(): CategoriesJson =
        simpleGet("partners/categories")

    override suspend fun getPartners(): PartnersJson =
        simpleGet("partners/search") {
            parameter("city", "AMS")
            parameter("per_page", "5000")
            parameter("radius", "2000")
            // category_ids
            // zipcode
            // query
        }

//    suspend fun search(params: SearchParams): SearchResultsJson {
//        log.debug { "GET /workouts/search ($params)" }
//        val respond = client.get("workouts/search") {
//            parameter("city", params.city)
//            parameter("limit", params.limit)
//            parameter("page", params.page)
//            parameter("start", params.start)
//            parameter("end", params.end)
//        }
//        respond.requireOk()
//        return respond.body()
//    }

//    suspend fun getReservations(): ReservationsJson {
//        log.debug { "GET /members/schedule/reservations" }
//        val respond = client.get("members/schedule/reservations") {
//
//        }
//        respond.requireOk()
//        return respond.body()
//    }

}


private suspend fun HttpResponse.requireOk() {
    if (status != HttpStatusCode.OK) {
        System.err.println(bodyAsText())
        error("Invalid status code: $status (${request.url})")
    }
}
