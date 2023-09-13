package allfit.service.credentials

import allfit.api.Credentials
import allfit.domain.Location
import allfit.domain.LocationAnd
import io.github.oshai.kotlinlogging.KotlinLogging.logger

object CredentialsManager {

    private val log = logger {}

    fun load(): LocationAnd<Credentials> {
        val loaded = CredentialsStorage.load()
        return if (loaded == null) {
            requestCredentialsViaUiAndSave()
        } else {
            LocationAnd<Credentials>(null, loaded)
        }
    }

    fun requestToStoreNewCredentialsOnError() {
        log.debug { "Storing new credentials." }
        val defaultEmail = CredentialsStorage.load()?.email ?: ""
        val newCreds = requestCredentialsViaUi(CredentialsMode.UpdateOnError(defaultEmail))
        CredentialsStorage.save(newCreds.other)
    }

    private fun requestCredentialsViaUiAndSave(): LocationAnd<Credentials> {
        val credentials = requestCredentialsViaUi(CredentialsMode.InitialSet)
        CredentialsStorage.save(credentials.other)
        return credentials
    }

    private fun requestCredentialsViaUi(mode: CredentialsMode): LocationAnd<Credentials> {
        var newCredentials: Credentials? = null
        var newLocation: Location? = null
        val dialog = CredentialsInitDialog(mode) { creds, loc ->
            newCredentials = creds
            newLocation = loc
        }
        dialog.show()
        newCredentials?.let {
            return LocationAnd(newLocation, it)
        } ?: error("You have to specify credentials as they are mandatory to run the application.")
    }

}
