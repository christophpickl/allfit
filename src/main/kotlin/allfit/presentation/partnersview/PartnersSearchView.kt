package allfit.presentation.partnersview

import allfit.presentation.PartnerSearchFXEvent
import allfit.presentation.models.FullPartner
import allfit.presentation.search.SearchRequest
import allfit.presentation.search.SearchView
import tornadofx.FXEvent
import tornadofx.hbox
import tornadofx.vbox

class PartnersSearchView : SearchView<FullPartner>(SearchRequest.alwaysTrue()) {

    override val root = hbox {
        vbox {
//            addIt(PartnerCheckinSearchPane(::checkSearch))
        }
//        vbox {
//            addIt(IsWorkoutReservedSearchPane(::checkSearch))
//        }
    }

    override fun buildEvent(request: SearchRequest<FullPartner>): FXEvent =
        PartnerSearchFXEvent(request)
}