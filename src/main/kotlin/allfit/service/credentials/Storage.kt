package allfit.service.credentials

import allfit.api.Credentials
import allfit.service.FileEntry
import allfit.service.FileResolver
import allfit.service.kotlinxSerializer
import io.github.oshai.kotlinlogging.KotlinLogging.logger
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString

object CredentialsStorage {

    private val log = logger {}
    private val encrypter = Encrypter()

    fun save(credentials: Credentials) {
        log.debug { "Storing to file: $credentials" }
        loginFile().writeText(
            kotlinxSerializer.encodeToString(
                LoginJson(
                    email = credentials.email,
                    password = encrypter.encrypt(credentials.clearTextPassword),
                )
            )
        )
    }

    fun load(): Credentials? {
        val loginFile = loginFile()
        log.debug { "Loading credentials from: ${loginFile.absolutePath}" }
        return if (loginFile.exists()) {
            val json = kotlinxSerializer.decodeFromString<LoginJson>(loginFile.readText())
            Credentials(
                email = json.email,
                clearTextPassword = encrypter.decrypt(json.password),
            )
        } else {
            null
        }
    }

    private fun loginFile() = FileResolver.resolve(FileEntry.Login)

}

@Serializable
private data class LoginJson(
    val email: String,
    /** Encrypted password */
    val password: String,
)
