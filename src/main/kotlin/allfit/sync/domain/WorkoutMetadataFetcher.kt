package allfit.sync.domain

import allfit.api.OnefitUtils
import io.github.oshai.kotlinlogging.KotlinLogging.logger
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import java.io.IOException
import java.nio.channels.UnresolvedAddressException
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.delay
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.select.Elements

interface WorkoutMetadataFetcher {
    suspend fun fetch(url: WorkoutUrl, listener: WorkoutMetadataFetchListener): WorkoutFetchMetadata
}

class DummyWorkoutFetcher : WorkoutMetadataFetcher {
    var fetched = WorkoutFetchMetadata(42, "about", "specifics", "address", null, emptyList())
    override suspend fun fetch(url: WorkoutUrl, listener: WorkoutMetadataFetchListener) =
        fetched.copy(workoutId = url.workoutId)
}

data class WorkoutUrl(
    val workoutId: Int,
    val workoutSlug: String,
) {
    val url = OnefitUtils.workoutUrl(workoutId, workoutSlug)
}

data class WorkoutFetchMetadata(
    val workoutId: Int,
    override val about: String,
    override val specifics: String,
    override val address: String,
    override val teacher: String?,
    val imageUrls: List<String>, // add "?w=123" to define width
) : WorkoutMetadata {
    companion object {
        fun empty(workoutId: Int) = WorkoutFetchMetadata(
            workoutId = workoutId, about = "", specifics = "", address = "", imageUrls = emptyList(), teacher = null
        )
    }
}

interface WorkoutMetadata {
    val about: String
    val specifics: String
    val address: String
    val teacher: String?
}

interface WorkoutMetadataFetchListener {
    fun failedFetching(message: String)
}

class HttpWorkoutMetadataFetcher : WorkoutMetadataFetcher {

    private val log = logger {}
    private val client = HttpClient()
    private val maxRetries = 10
    private val pauseBetweenRetries = 500.milliseconds

    override suspend fun fetch(url: WorkoutUrl, listener: WorkoutMetadataFetchListener): WorkoutFetchMetadata {
        log.debug { "Fetch workout metadata from: ${url.url}" }
        return fetchRetriable(url, listener, 1)
    }

    private suspend fun fetchRetriable(
        url: WorkoutUrl,
        listener: WorkoutMetadataFetchListener,
        attempt: Int
    ): WorkoutFetchMetadata {
        val response: HttpResponse
        try {
            response = client.get(url.url)
        } catch (e: Exception) {
            if (e is IOException || e is UnresolvedAddressException || e is ClosedReceiveChannelException) {
                log.warn(e) { "Retrying to fetch URL: ${url.url} (attempt: ${attempt + 1}/$maxRetries)" }
                return fetchRetriable(url, listener, attempt + 1)
            } else {
                throw e
            }
        }
        return when (response.status.value) {
            200 -> WorkoutHtmlParser.parse(url.workoutId, response.bodyAsText())
            404 -> WorkoutFetchMetadata.empty(url.workoutId)
            in 500..599 -> {
                if (attempt == maxRetries) {
                    val errorMessage =
                        "Invalid response (${response.status.value}) after last attempt $maxRetries for URL: ${url.url}"
                    log.warn { errorMessage }
                    log.warn { "Going to ignore it and return empty workout data instead." }
                    listener.failedFetching("No metadata could be fetched (${response.status.value}) for workout at: ${url.url}")
                    WorkoutFetchMetadata.empty(url.workoutId)
                } else {
                    log.warn { "Retrying after receiving ${response.status} to fetch URL: ${url.url} (attempt: ${attempt + 1}/${maxRetries})" }
                    delay(pauseBetweenRetries)
                    fetchRetriable(url, listener, attempt + 1)
                }
            }

            else -> error("Invalid HTTP response ${response.status} for URL: ${url.url}")
        }
    }
}

object WorkoutHtmlParser {
    fun parse(workoutId: Int, html: String): WorkoutFetchMetadata {
        val body = Jsoup.parse(html).body()
        return WorkoutFetchMetadata(
            workoutId = workoutId,
            about = parseAbout(body),
            specifics = parseSpecifics(body),
            address = parseAddress(body),
            imageUrls = parseImageUrls(body),
            teacher = parseTeacher(body),
        )
    }

    private fun parseAbout(body: Element): String {
        val about = body.getElementsByClass("about")
        if (about.size == 0) return ""
        return about.requireOneChild().select(".readMore--aboutLesson > div:nth-child(1) > p:nth-child(1)")[0].html()
    }

    private fun parseSpecifics(body: Element): String {
        val specifics = body.getElementsByClass("specifics")
        if (specifics.size == 0) return ""
        return specifics.requireOneChild().select(".readMore--specifics > div:nth-child(1) > p:nth-child(1)")[0].html()
    }

    private fun parseAddress(body: Element) = body.select("div.address").text()

    private fun parseImageUrls(body: Element) = body.select("div.inventoryDetailHero__gallery__nav__element").map {
        val img = it.getElementsByTag("img").requireOneChild()[0]
        img.attr("src").substringBefore("?")
    }

    private fun parseTeacher(body: Element): String? {
        val names = body.getElementsByClass("teacher__name")
        if (names.size == 0) return null
        return names.requireOneChild().text()
    }
}

private fun Elements.requireOneChild() = apply {
    require(size == 1) { "Expected element to have single child but had: $size" }
}
