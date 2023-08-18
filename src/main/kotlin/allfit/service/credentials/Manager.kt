package allfit.service.credentials

import allfit.api.Credentials
import io.github.oshai.kotlinlogging.KotlinLogging.logger

object CredentialsManager {

    private val log = logger {}

    fun load(): Credentials =
        CredentialsStorage.load() ?: requestCredentialsViaUiAndSave()

    fun requestToStoreNewCredentialsOnError() {
        log.debug { "Storing new credentials." }
        val defaultEmail = CredentialsStorage.load()?.email ?: ""
        val newCreds = requestCredentialsViaUi(CredentialsMode.UpdateOnError(defaultEmail))
        CredentialsStorage.save(newCreds)
    }

    private fun requestCredentialsViaUiAndSave(): Credentials {
        val credentials = requestCredentialsViaUi(CredentialsMode.InitialSet)
        CredentialsStorage.save(credentials)
        return credentials
    }

    private fun requestCredentialsViaUi(mode: CredentialsMode): Credentials {
        var newCredentials: Credentials? = null
        val dialog = CredentialsInitDialog(mode) {
            newCredentials = it
        }
        dialog.show()
        newCredentials?.let {
            return it
        } ?: error("You have to specify credentials as they are mandatory to run the application.")
    }

}
