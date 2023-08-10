package allfit.service

import allfit.api.Credentials
import io.github.oshai.kotlinlogging.KotlinLogging.logger
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString

object CredentialsLoader {

    private val log = logger {}

    fun load(): Credentials {
        val loginFile = FileResolver.resolve(FileEntry.Login)
        log.debug { "Loading credentials from: ${loginFile.absolutePath}" }
        if (!loginFile.exists()) {
            error(
                "Expected login file existing at:\n${loginFile.absolutePath}\n\n" +
                        "I created a template for you for now, please fill in your username and password."
            )
        }
        return kotlinxSerializer.decodeFromString<LoginJson>(loginFile.readText()).toCredentials()
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
