package allfit.service

import io.github.oshai.kotlinlogging.KotlinLogging.logger
import java.util.concurrent.ConcurrentLinkedQueue
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
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
    val workList = this
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
                        val leftOver = workList.size - items.size
                        percentageProgressCallback(leftOver.toDouble() / workList.size)
                        lastPercentageBroadcasted = now
                    }
                }
            }
        }.joinAll()
    }
    percentageProgressCallback(100.0)
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
