package allfit.service

import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.client.statement.request
import io.ktor.http.HttpStatusCode
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import mu.KotlinLogging.logger

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
