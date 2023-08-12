package allfit.api

import allfit.api.models.AuthJson
import allfit.api.models.AuthResponseJson
import allfit.api.models.CategoriesJsonRoot
import allfit.api.models.CheckinJson
import allfit.api.models.CheckinsJsonRoot
import allfit.api.models.MetaJson
import allfit.api.models.PartnersJsonRoot
import allfit.api.models.ReservationsJsonRoot
import allfit.api.models.SingleWorkoutJsonRoot
import allfit.api.models.UsageJsonRoot
import allfit.api.models.WorkoutJson
import allfit.api.models.WorkoutsJsonRoot
import allfit.service.Clock
import allfit.service.FileEntry
import allfit.service.FileResolver
import allfit.service.beginOfDay
import allfit.service.endOfDay
import allfit.service.kotlinxSerializer
import allfit.service.requireOk
import allfit.service.toPrettyString
import io.github.oshai.kotlinlogging.KotlinLogging.logger
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

private val log = logger {}
private val authClient = buildClient(null)

suspend fun authenticateOneFit(
    credentials: Credentials, clock: Clock
): OnefitClient {
    val response = authClient.post("") {
        header("Content-Type", "application/json")
        setBody(
            AuthJson(
                email = credentials.email,
                password = credentials.password,
            )
        )
    }
    if (response.status == HttpStatusCode.Unauthorized) {
        val loginFile = FileResolver.resolve(FileEntry.Login)
        error(
            "Authenticating as '${credentials.email}' failed!\n\nPlease verify your credentials by logging in to OneFit directly,\n" + "and check the entered password in the login file here:\n${loginFile.absolutePath}"
        )
    }
    response.requireOk()
    log.info { "Login success." }
    return OnefitHttpClient(response.body<AuthResponseJson>().access_token, clock = clock)
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
        header("X-ONEFIT-CLIENT", "website/29.14.4")
        if (authToken != null) {
            bearerAuth(authToken)
        }
    }
}

class OnefitHttpClient(
    authToken: String,
    private val clock: Clock,
    private val jsonLogFileManager: JsonLogFileManager = JsonLogFileManagerImpl(),
) : OnefitClient {

    private val log = logger {}
    private val client = buildClient(authToken)

    override suspend fun getCategories(): CategoriesJsonRoot = get("partners/categories")

    override suspend fun getPartners(params: PartnerSearchParams): PartnersJsonRoot =
        // slightly different result from partners/city/AMS, but this one is better ;)
        get("partners/search") {
            parameter("city", params.city)
            parameter("per_page", params.pageItemCount)
//            parameter("radius", params.radiusInMeters)
//            parameter("zipcode", params.zipCode)
            // category_ids
            // query
        }

    override suspend fun getWorkouts(params: WorkoutSearchParams): List<WorkoutJson> {
        log.debug { "Fetching paged response with params: $params" }

        val data = mutableListOf<WorkoutJson>()

        var lastMeta: MetaJson
        (0..<params.totalDays).forEach { dayCount ->
            val dayCountL = dayCount.toLong()
            log.debug { "Fetching workouts for day ${dayCount + 1} / ${params.totalDays}" }
            var currentParams =
                params.copy(start = params.start.plusDays(dayCountL), end = params.start.plusDays(dayCountL))
            if (dayCount != 0) {
                currentParams = currentParams.copy(start = currentParams.start.beginOfDay())
            }
            if (dayCount != (params.totalDays - 1)) {
                currentParams = currentParams.copy(end = currentParams.end.endOfDay())
            }

//            println("params: $currentParams")
            do {
                val result = getWorkoutsPage(currentParams)
                data += result.data
                currentParams = currentParams.nextPage()
                lastMeta = result.meta
                log.debug { "Received meta info for page: ${lastMeta.pagination}" }
            } while (currentParams.page <= lastMeta.pagination.total_pages)
        }

        return data
    }

    override suspend fun getWorkoutById(id: Int): SingleWorkoutJsonRoot = get("workouts/$id")

    override suspend fun getReservations(): ReservationsJsonRoot = get("members/schedule/reservations")

    override suspend fun getCheckins(params: CheckinSearchParams): CheckinsJsonRoot {
        log.debug { "Fetching paged response with params: $params" }
        val data = mutableListOf<CheckinJson>()
        var currentParams = params
        var lastMeta: MetaJson
        do {
            val result = getCheckinsPage(currentParams)
            data += result.data
            currentParams = currentParams.nextPage()
            lastMeta = result.meta
            log.debug { "Received meta info for page: ${lastMeta.pagination}" }
        } while (currentParams.page <= lastMeta.pagination.total_pages)
        return CheckinsJsonRoot(data, lastMeta)
    }

    override suspend fun getUsage(): UsageJsonRoot = get("members/usage")

    private suspend fun getCheckinsPage(params: CheckinSearchParams): CheckinsJsonRoot = get("members/check-ins") {
        parameter("limit", params.limit)
        parameter("page", params.page)
    }

    private suspend fun getWorkoutsPage(params: WorkoutSearchParams): WorkoutsJsonRoot =
        get<WorkoutsJsonRoot>("workouts/search") {
            parameter("city", params.city)
            parameter("limit", params.limit)
            parameter("page", params.page)
            parameter("start", params.startFormatted)
            parameter("end", params.endFormatted)
            parameter("is_digital", params.isDigital)
        }

    private suspend inline fun <reified T> get(
        path: String, requestModifier: HttpRequestBuilder.() -> Unit = {}
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
        jsonLogFileManager.save(
            JsonLogFileName(path, status.value, clock.now()), kotlinxSerializer.toPrettyString(bodyAsText())
        )
    }

}