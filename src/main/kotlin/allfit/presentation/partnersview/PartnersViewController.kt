package allfit.presentation.partnersview

import allfit.persistence.domain.UsageRepository
import allfit.presentation.models.FullPartner
import allfit.presentation.models.PartnersViewModel
import allfit.presentation.models.toUsage
import tornadofx.Controller

class PartnersViewController : Controller() {

    private val partnersModel: PartnersViewModel by inject()
    private val usageRepo: UsageRepository by di()

    init {
        val usage = usageRepo.selectOne().toUsage()
        partnersModel.selectedPartner.initPartner(FullPartner.prototype, usage)

        // EVENT: partnerDetailModel.selectedPartner.initPartner(dataStorage.getPartnerById(workout.partner.id), usage)
    }
}
