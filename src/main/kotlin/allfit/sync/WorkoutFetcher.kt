package allfit.sync

import allfit.service.requireOk
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import mu.KotlinLogging.logger
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.select.Elements

interface WorkoutFetcher {
    suspend fun fetch(url: WorkoutUrl): WorkoutFetch
}

class DummyWorkoutFetcher : WorkoutFetcher {
    var fetched = WorkoutFetch(42, "about", "specifics", "address", emptyList())
    override suspend fun fetch(url: WorkoutUrl) = fetched.copy(workoutId = url.workoutId)
}

data class WorkoutUrl(
    val workoutId: Int,
    val workoutSlug: String,
) {
    val url = "https://one.fit/en-nl/workouts/$workoutId/$workoutSlug"
}

data class WorkoutFetch(
    val workoutId: Int,
    override val about: String,
    override val specifics: String,
    override val address: String,
    val imageUrls: List<String>, // add "?w=123" to define width
) : WorkoutHtmlMetaData

class WorkoutFetcherImpl : WorkoutFetcher {
    private val log = logger {}
    private val client = HttpClient()
    override suspend fun fetch(url: WorkoutUrl): WorkoutFetch {
        log.debug { "Fetch workout data from: ${url.url}" }
        val response = client.get(url.url)
        response.requireOk()
        return WorkoutHtmlParser.parse(url.workoutId, response.bodyAsText())
    }
}

object WorkoutHtmlParser {
    fun parse(workoutId: Int, html: String): WorkoutFetch {
        val body = Jsoup.parse(html).body()
        return WorkoutFetch(
            workoutId = workoutId,
            about = parseAbout(body),
            specifics = parseSpecifics(body),
            address = parseAddress(body),
            imageUrls = parseImageUrls(body),
        )
    }

    private fun parseAbout(body: Element): String {
        val about = body.getElementsByClass("about")
        if (about.size == 0) return ""
        return about.requireOneChild()
            .select(".readMore--aboutLesson > div:nth-child(1) > p:nth-child(1)")[0].html()
    }

    private fun parseSpecifics(body: Element) =
        body.getElementsByClass("specifics").requireOneChild()
            .select(".readMore--specifics > div:nth-child(1) > p:nth-child(1)")[0].html()

    private fun parseAddress(body: Element) =
        body.select("div.address").text()

    private fun parseImageUrls(body: Element) =
        body.select("div.inventoryDetailHero__gallery__nav__element").map {
            val img = it.getElementsByTag("img").requireOneChild()[0]
            img.attr("src").substringBefore("?")
        }
}

private fun Elements.requireOneChild() = apply {
    require(size == 1) { "Expected element to have single child but had: $size" }
}
