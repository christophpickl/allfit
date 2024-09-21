package allfit.presentation.partners

import allfit.presentation.PartnerSearchFXEvent
import allfit.presentation.PartnerSelectedFXEvent
import allfit.presentation.logic.DataStorage
import allfit.presentation.models.FullWorkout
import allfit.presentation.models.UsageModel
import allfit.presentation.tornadofx.safeSubscribe
import io.github.oshai.kotlinlogging.KotlinLogging.logger
import tornadofx.Controller

class PartnersController : Controller() {

    private val logger = logger {}
    private val partnersModel: PartnersViewModel by inject()
    private val usageModel: UsageModel by inject()
    private val dataStorage: DataStorage by di()

    init {
        safeSubscribe<PartnerSelectedFXEvent> {
            val selectedPartnerId = it.partnerId
            val selectedPartner = dataStorage.getPartnerById(selectedPartnerId)
            logger.debug { "partner selected: id=$selectedPartnerId, name=${selectedPartner.name}" }
            if(partnersModel.selectedPartner.id.get() != selectedPartnerId) {
                // only reset workout if user selected a different partner (otherwise updated it, and staid the same)
                partnersModel.selectedWorkout.set(FullWorkout.prototype)
            }
            partnersModel.selectedPartner.initPartner(selectedPartner, usageModel.usage.get())
        }
        safeSubscribe<PartnerSearchFXEvent> {
            logger.debug { "Search: ${it.searchRequest}" }
            partnersModel.sortedFilteredPartners.predicate = it.searchRequest.predicate
        }
    }
}
