package allfit.presentation.logic

import allfit.presentation.HidePartnerFXEvent
import allfit.presentation.UnhidePartnerFXEvent
import allfit.presentation.UpdatePartnerFXEvent
import allfit.presentation.models.MainViewModel
import allfit.presentation.models.PartnersViewModel
import allfit.presentation.tornadofx.safeSubscribe
import mu.KotlinLogging.logger
import tornadofx.Controller

class PartnerUpdateController : Controller() {

    private val logger = logger {}
    private val mainModel: MainViewModel by inject()
    private val partnersModel: PartnersViewModel by inject()
    private val dataStorage: DataStorage by di()

    init {
        safeSubscribe<UpdatePartnerFXEvent>() {
            logger.debug { "Updating partner: ${it.modifications}" }
            dataStorage.updatePartner(it.modifications)
            mainModel.sortedFilteredWorkouts.refilter()
            partnersModel.sortedFilteredPartners.refilter()
        }
        safeSubscribe<HidePartnerFXEvent>() {
            logger.debug { "Received HidePartnerFXEvent: ${it.partnerId}" }
            dataStorage.hidePartner(it.partnerId)
            mainModel.sortedFilteredWorkouts.refilter()
            partnersModel.sortedFilteredPartners.refilter()
        }
        safeSubscribe<UnhidePartnerFXEvent>() {
            logger.debug { "Received UnhidePartnerFXEvent: ${it.partnerId}" }
            dataStorage.unhidePartner(it.partnerId)
            mainModel.sortedFilteredWorkouts.refilter()
            partnersModel.sortedFilteredPartners.refilter()
        }
    }
}