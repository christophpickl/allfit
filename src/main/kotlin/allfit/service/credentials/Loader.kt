package allfit.service.credentials

import allfit.api.Credentials
import allfit.service.FileEntry
import allfit.service.FileResolver
import allfit.service.kotlinxSerializer
import io.github.oshai.kotlinlogging.KotlinLogging.logger
import java.io.File
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString

object CredentialsLoader {

    private val log = logger {}
    private val encrypter = Encrypter()

    fun load(): Credentials {
        val loginFile = FileResolver.resolve(FileEntry.Login)
        log.debug { "Loading credentials from: ${loginFile.absolutePath}" }
        return if (loginFile.exists()) {
            loadCredentailsFromFile(loginFile)
        } else {
            loadCredentialsFromUiAndStoreInFile()
        }
    }

    private fun loadCredentailsFromFile(loginFile: File): Credentials {
        val json = kotlinxSerializer.decodeFromString<LoginJson>(loginFile.readText())
        return Credentials(
            email = json.email,
            clearTextPassword = encrypter.decrypt(json.password),
        )
    }

    private fun loadCredentialsFromUiAndStoreInFile(): Credentials {
        val credentials = loadCredentialsFromUi()
        storeCredentialsToFile(credentials)
        return credentials
    }

    private fun loadCredentialsFromUi(): Credentials {
        var newCredentials: Credentials? = null
        val dialog = CredentialsInitDialog {
            newCredentials = it
        }
        dialog.show()
        newCredentials?.let {
            return it
        } ?: error("You have to specify credentials as they are mandatory to run the application.")
    }

    private fun storeCredentialsToFile(credentials: Credentials) {
        val loginFile = FileResolver.resolve(FileEntry.Login)
        loginFile.writeText(
            kotlinxSerializer.encodeToString(
                LoginJson(
                    email = credentials.email,
                    password = encrypter.encrypt(credentials.clearTextPassword),
                )
            )
        )
    }
}

@Serializable
private data class LoginJson(
    val email: String,
    /** Encrypted password */
    val password: String,
)
