package allfit.api

import allfit.api.models.AuthJson
import allfit.api.models.AuthResponseJson
import allfit.api.models.CategoriesJson
import allfit.api.models.MetaJson
import allfit.api.models.MetaPaginationJson
import allfit.api.models.PagedJson
import allfit.api.models.PartnersJson
import allfit.api.models.WorkoutsJson
import allfit.service.formatOnefit
import allfit.service.readApiResponse
import allfit.service.zone
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
import java.time.ZonedDateTime

interface OnefitClient {

    suspend fun getCategories(): CategoriesJson
    suspend fun getPartners(params: PartnerSearchParams): PartnersJson
    suspend fun getWorkouts(params: WorkoutSearchParams): WorkoutsJson

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

class InMemoryOnefitClient : OnefitClient {
    var categoriesJson = CategoriesJson(emptyList())
    var partnersJson = PartnersJson(emptyList())
    var workoutsJson = WorkoutsJson(emptyList(), MetaJson(MetaPaginationJson(1, 1)))
    override suspend fun getCategories() = categoriesJson
    override suspend fun getPartners(params: PartnerSearchParams) = partnersJson
    override suspend fun getWorkouts(params: WorkoutSearchParams) = workoutsJson
}

object ClassPathOnefitClient : OnefitClient {
    override suspend fun getCategories() =
        readApiResponse<CategoriesJson>("partners_categories.json")

    override suspend fun getPartners(params: PartnerSearchParams) =
        readApiResponse<PartnersJson>("partners_search.json")

    override suspend fun getWorkouts(params: WorkoutSearchParams) =
        readApiResponse<WorkoutsJson>("workouts_search.json")
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


    override suspend fun getCategories(): CategoriesJson =
        get("partners/categories")

    override suspend fun getPartners(params: PartnerSearchParams): PartnersJson =
        // slightly different result from partners/city/AMS, but this one is better ;)
        get("partners/search") {
            parameter("city", params.city)
            parameter("per_page", params.pageItemCount)
            parameter("radius", params.radiusInMeters)
            parameter("zipcode", params.zipCode)
            // category_ids
            // query
        }

    override suspend fun getWorkouts(params: WorkoutSearchParams): WorkoutsJson =
        getPaged(params, ::getWorkoutsPage) { data, meta ->
            WorkoutsJson(data, meta)
        }

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
        response.requireOk()
        return response.body()
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

interface PagedParams<THIS : PagedParams<THIS>> {
    val limit: Int
    val page: Int
    fun nextPage(): THIS
}

data class PartnerSearchParams(
    val city: String,
    val pageItemCount: Int,
    val radiusInMeters: Int,
    val zipCode: String,
) {
    companion object {
        fun simple() =
            PartnerSearchParams(
                city = "AMS",
                pageItemCount = 5_000,
                radiusInMeters = 3_000,
                zipCode = "1011HW",
            )
    }
}

data class WorkoutSearchParams(
    val city: String,
    override val limit: Int,
    override val page: Int,
    val start: ZonedDateTime,
    val end: ZonedDateTime,
    val isDigital: Boolean
) : PagedParams<WorkoutSearchParams> {

    val startFormatted = start.formatOnefit()
    val endFormatted = end.formatOnefit()

    override fun nextPage() = copy(page = page + 1)

    companion object {
        fun simple(
            limit: Int = 10_000
        ): WorkoutSearchParams {
            val now = ZonedDateTime.now(zone).withHour(0).withMinute(0).withSecond(0)
            return WorkoutSearchParams(
                city = "AMS",
                limit = limit,
                page = 1,
                start = now,
                end = now.plusDays(2),
                isDigital = false,
            )
        }
    }
}

private suspend fun HttpResponse.requireOk() {
    if (status != HttpStatusCode.OK) {
        System.err.println(bodyAsText())
        error("Invalid status code: $status requesting ${request.url}")
    }
}
