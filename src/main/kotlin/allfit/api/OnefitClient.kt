package allfit.api

import allfit.api.models.PartnerCategoriesJson
import allfit.readApiResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
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

    suspend fun getPartnersCategories(): PartnerCategoriesJson

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
    override suspend fun getPartnersCategories() =
        readApiResponse<PartnerCategoriesJson>("partners_categories.json")
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
        url(if (authToken == null) "https://one.fit/api/auth" else "https://api.one.fit/v2/nl-nl/")
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

    override suspend fun getPartnersCategories(): PartnerCategoriesJson {
        log.debug { "GET /partners/categories" }
        val respond = client.get("partners/categories")
        respond.requireOk()
        return respond.body()
    }

    suspend fun search(params: SearchParams): SearchResultsJson {
        log.debug { "GET /workouts/search ($params)" }
        val respond = client.get("workouts/search") {
            parameter("city", params.city)
            parameter("limit", params.limit)
            parameter("page", params.page)
            parameter("start", params.start)
            parameter("end", params.end)
        }
        respond.requireOk()
        return respond.body()
    }

    // TODO
//    suspend fun getReservations(): ReservationsJson {
//        log.debug { "GET /members/schedule/reservations" }
//        val respond = client.get("members/schedule/reservations") {
//
//        }
//        respond.requireOk()
//        return respond.body()
//    }

    suspend fun getUsage(): UsagesJson {
        log.debug { "GET /members/usage" }
        val respond = client.get("members/usage")
        respond.requireOk()
        return respond.body()
    }
}


private suspend fun HttpResponse.requireOk() {
    if (status != HttpStatusCode.OK) {
        System.err.println(bodyAsText())
        error("Invalid status code: $status (${request.url})")
    }
}
