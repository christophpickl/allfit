package allfit

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

inline fun <reified T> readApiResponse(fileName: String): T {
    val classpath = "/api_responses/$fileName"
    val resource = T::class.java.getResourceAsStream(classpath)
        ?: throw Exception("Classpath resource not found at: $classpath")
    val json = resource.bufferedReader()
        .use {
            it.readText()
        }
    return Json.decodeFromString(json)
}
