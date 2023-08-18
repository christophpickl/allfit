package allfit.service.credentials

import allfit.api.Credentials
import allfit.service.FileEntry
import allfit.service.FileResolver
import allfit.service.kotlinxSerializer
import io.github.oshai.kotlinlogging.KotlinLogging.logger
import java.io.File
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString

object CredentialsManager {

    private val log = logger {}
    private val encrypter = Encrypter()

    fun load(): Credentials {
        val loginFile = loginFile()
        log.debug { "Loading credentials from: ${loginFile.absolutePath}" }
        return if (loginFile.exists()) {
            loadCredentailsFromFile(loginFile)
        } else {
            loadCredentialsFromUiAndStoreInFile()
        }
    }

    fun requestToStoreNewCredentialsOnError() {
        val loginFile = loginFile()
        val defaultEmail = if (loginFile.exists()) loadCredentailsFromFile(loginFile).email else ""
        val newCreds = loadCredentialsFromUi(CredentialsMode.UpdateOnError(defaultEmail))
        storeCredentialsToFile(newCreds)
    }

    private fun loadCredentailsFromFile(loginFile: File): Credentials {
        val json = kotlinxSerializer.decodeFromString<LoginJson>(loginFile.readText())
        return Credentials(
            email = json.email,
            clearTextPassword = encrypter.decrypt(json.password),
        )
    }

    private fun loadCredentialsFromUiAndStoreInFile(): Credentials {
        val credentials = loadCredentialsFromUi(CredentialsMode.InitialSet)
        storeCredentialsToFile(credentials)
        return credentials
    }

    private fun loadCredentialsFromUi(mode: CredentialsMode): Credentials {
        var newCredentials: Credentials? = null
        val dialog = CredentialsInitDialog(mode) {
            newCredentials = it
        }
        dialog.show()
        newCredentials?.let {
            return it
        } ?: error("You have to specify credentials as they are mandatory to run the application.")
    }

    private fun storeCredentialsToFile(credentials: Credentials) {
        log.debug { "Storing to file: $credentials" }
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

    private fun loginFile() = FileResolver.resolve(FileEntry.Login)

}

@Serializable
private data class LoginJson(
    val email: String,
    /** Encrypted password */
    val password: String,
)
