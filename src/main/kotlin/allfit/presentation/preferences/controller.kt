package allfit.presentation.preferences

import allfit.domain.Location
import allfit.persistence.domain.PreferencesData
import allfit.persistence.domain.SinglesRepo
import io.github.oshai.kotlinlogging.KotlinLogging.logger
import tornadofx.Controller

class PreferencesController : Controller() {

    private val maxSyncDays = 14
    private val logger = logger {}
    private val model: PreferencesModel by inject()
    private val singlesRepo: SinglesRepo by di()

    init {
        val prefs = singlesRepo.selectPreferencesData()
        model.location.set(prefs.location)
        model.syncDays.set(prefs.syncDays)
        model.locationOptions.addAll(Location.entries)
        model.syncDaysOptions.addAll((1..maxSyncDays).toList())

        subscribe<SavePreferencesFXEvent> {
            logger.debug { "Received SavePreferencesFXEvent." }
            singlesRepo.updatePreferencesData(
                PreferencesData(
                    location = model.location.get(),
                    syncDays = model.syncDays.get(),
                )
            )
        }
    }
}
