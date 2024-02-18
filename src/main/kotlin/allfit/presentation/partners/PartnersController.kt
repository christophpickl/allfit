package allfit.presentation.partners

import allfit.presentation.PartnerAddedFXEvent
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
            val partnerId = it.partnerId
            logger.debug { "Change partner: $partnerId" }
            partnersModel.selectedPartner.initPartner(dataStorage.getPartnerById(partnerId), usageModel.usage.get())
            partnersModel.selectedWorkout.set(FullWorkout.prototype)
        }
        safeSubscribe<PartnerAddedFXEvent> {
            logger.debug { "Partner added: '${it.partner.name}'" }
            partnersModel.sortedFilteredPartners.add(it.partner)
        }
        safeSubscribe<PartnerSearchFXEvent> {
            logger.debug { "Search: ${it.searchRequest}" }
            partnersModel.sortedFilteredPartners.predicate = it.searchRequest.predicate
        }
    }
}
