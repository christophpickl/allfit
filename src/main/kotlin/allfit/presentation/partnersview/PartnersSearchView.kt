package allfit.presentation.partnersview

import allfit.presentation.PartnerSearchFXEvent
import allfit.presentation.models.FullPartner
import allfit.presentation.search.CheckinSearchPane
import allfit.presentation.search.RatingSearchPane
import allfit.presentation.search.SearchRequest
import allfit.presentation.search.SearchView
import allfit.presentation.search.TextSearchPane
import tornadofx.FXEvent
import tornadofx.hbox
import tornadofx.vbox

class PartnersSearchView : SearchView<FullPartner>(SearchRequest.alwaysTrue()) {

    override val root = hbox {
        vbox {
            addIt(TextSearchPane(::checkSearch))
            addIt(CheckinSearchPane(::checkSearch))
            addIt(RatingSearchPane(::checkSearch))
        }
    }

    override fun buildEvent(request: SearchRequest<FullPartner>): FXEvent =
        PartnerSearchFXEvent(request)
}