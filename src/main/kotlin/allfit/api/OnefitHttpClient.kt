package allfit.api

import allfit.api.models.AuthJson
import allfit.api.models.AuthResponseJson
import allfit.api.models.CategoriesJson
import allfit.api.models.MetaJson
import allfit.api.models.PagedJson
import allfit.api.models.PartnersJson
import allfit.api.models.ReservationsJson
import allfit.api.models.WorkoutsJson
import allfit.service.DirectoryEntry
import allfit.service.FileResolver
import allfit.service.kotlinxSerializer
import allfit.service.requireOk
import allfit.service.toPrettyString
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
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import java.io.File

private val log = KotlinLogging.logger {}
private val authClient = buildClient(null)

suspend fun authenticateOneFit(credentials: Credentials): OnefitClient {
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
    return OnefitHttpClient(response.body<AuthResponseJson>().access_token)
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

class OnefitHttpClient(
    authToken: String,
) : OnefitClient {

    private val log = KotlinLogging.logger {}
    private val client = buildClient(authToken)


    override suspend fun getCategories(): CategoriesJson =
        get("partners/categories")

    override suspend fun getPartners(params: PartnerSearchParams): PartnersJson =
        // slightly different result from partners/city/AMS, but this one is better ;)
        get("partners/search") {
            parameter("city", params.city)
            parameter("per_page", params.pageItemCount)
//            parameter("radius", params.radiusInMeters)
//            parameter("zipcode", params.zipCode)
            // category_ids
            // query
        }

    override suspend fun getWorkouts(params: WorkoutSearchParams): WorkoutsJson =
        getPaged(params, ::getWorkoutsPage) { data, meta ->
            WorkoutsJson(data, meta)
        }

    override suspend fun getReservations(): ReservationsJson =
        get("members/schedule/reservations")

    private suspend fun getWorkoutsPage(params: WorkoutSearchParams): WorkoutsJson =
        get("workouts/search") {
            parameter("city", params.city)
            parameter("limit", params.limit)
            parameter("page", params.page)
            parameter("start", params.startFormatted)
            parameter("end", params.endFormatted)
            parameter("is_digital", params.isDigital)
        }

    private suspend inline fun <reified T> get(
        path: String,
        requestModifier: HttpRequestBuilder.() -> Unit = {}
    ): T {
        val response = client.get(path) {
            requestModifier()
        }
        log.debug { "${response.status.value} GET ${response.request.url}" }
        response.logJsonResponse(path)
        response.requireOk()
        return response.body()
    }

    private suspend fun HttpResponse.logJsonResponse(path: String) {
        val fileName = "${path.replace("/", "_")}-${status.value}.json"
        val jsonResponseString = kotlinxSerializer.toPrettyString(bodyAsText())
        File(FileResolver.resolve(DirectoryEntry.JsonLogs), fileName).writeText(jsonResponseString)
    }

    private suspend fun <
            JSON : PagedJson<ENTITY>,
            ENTITY,
            PARAMS : PagedParams<PARAMS>
            > getPaged(
        initParams: PARAMS,
        request: suspend (PARAMS) -> JSON,
        builder: (List<ENTITY>, MetaJson) -> JSON
    ): JSON {
        val data = mutableListOf<ENTITY>()

        var currentParams = initParams
        var lastMeta: MetaJson
        do {
            val result = request(currentParams)
            data += result.data
            currentParams = currentParams.nextPage()
            lastMeta = result.meta
            log.debug { "Received page ${lastMeta.pagination.current_page}/${lastMeta.pagination.total_pages}" }
        } while (currentParams.page <= lastMeta.pagination.total_pages)

        return builder(data, lastMeta)
    }
}