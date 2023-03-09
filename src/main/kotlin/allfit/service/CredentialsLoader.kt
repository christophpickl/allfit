package allfit.service

import allfit.api.Credentials
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import mu.KotlinLogging.logger

object CredentialsLoader {

    private val log = logger {}

    fun load(): Credentials {
        val file = FileResolver.resolve("login.json")
        log.debug { "Loading credentials from: ${file.absolutePath}" }
        if (!file.exists()) {
            error("Expected login file existing at: ${file.absolutePath}")
        }
        return Json.decodeFromString<LoginJson>(file.readText()).toCredentials()
    }
}

private fun LoginJson.toCredentials() = Credentials(
    email = email,
    password = password,
)

@Serializable
private data class LoginJson(
    val email: String,
    val password: String,
)
