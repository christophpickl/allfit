package allfit.service

import io.github.oshai.kotlinlogging.KotlinLogging.logger
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.client.statement.request
import io.ktor.http.HttpStatusCode
import java.util.concurrent.ConcurrentLinkedQueue
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val log = logger {}

val kotlinxSerializer = Json {
    ignoreUnknownKeys = true
    prettyPrint = true
}

fun readHtmlResponse(fileName: String): String =
    readFromClasspath("/html_responses/$fileName")

inline fun <reified T> readApiResponse(fileName: String): T {
    val json = readFromClasspath("/api_responses/$fileName")
    return kotlinxSerializer.decodeFromString(json)
}

private object ResourceLocator

fun readFromClasspath(classpath: String): String {
    val resource = ResourceLocator::class.java.getResourceAsStream(classpath)
        ?: throw Exception("Classpath resource not found at: $classpath")
    return resource.bufferedReader()
        .use {
            it.readText()
        }
}

fun Json.toPrettyString(jsonString: String) =
    encodeToString(parseToJsonElement(jsonString))

suspend fun HttpResponse.requireOk() {
    if (status != HttpStatusCode.OK) {
        System.err.println(bodyAsText())
        error("Invalid status code: $status requesting ${request.url}")
    }
}

suspend fun <T> List<T>.workParallel(
    numberOfCoroutines: Int,
    percentageProgressCallback: (Double) -> Unit = {},
    percentageBroadcastIntervalInMs: Long = 3_000,
    worker: suspend (T) -> Unit
) {
    log.debug { "Starting parallel job with $numberOfCoroutines coroutines for $size items ..." }
    require(numberOfCoroutines >= 1)
    percentageProgressCallback(0.0)
    var lastPercentageBroadcasted = System.currentTimeMillis()
    val items = ConcurrentLinkedQueue(toMutableList())
    runBlocking {
        (1..numberOfCoroutines).map { coroutine ->
            log.debug { "Starting coroutine $coroutine/$numberOfCoroutines ..." }
            launch {
                var item = items.poll()
                while (item != null) {
                    worker(item)
                    item = items.poll()
                    log.trace { "Items left to be processed: ${items.size}" }
                    val now = System.currentTimeMillis()
                    val msSinceLastMessage = now - lastPercentageBroadcasted
                    if (msSinceLastMessage > percentageBroadcastIntervalInMs) {
                        val leftOver = this@workParallel.size - items.size
                        percentageProgressCallback(leftOver.toDouble() / this@workParallel.size)
                        lastPercentageBroadcasted = now
                    }
                }
            }
        }.joinAll()
    }
}

data class Quadrupel<V1, V2, V3, V4>(
    val first: V1,
    val second: V2,
    val third: V3,
    val fourth: V4,
)

private const val tooLongIndicator = " ..."
fun String.ensureMaxLength(maxLength: Int): String {
    require(maxLength > 5)
    return if (length <= maxLength) this
    else substring(0, maxLength - tooLongIndicator.length) + tooLongIndicator
}
