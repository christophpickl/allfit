package allfit.presentation.partners

import allfit.presentation.PartnerSearchFXEvent
import allfit.presentation.models.FullPartner
import allfit.presentation.search.CheckinSearchPane
import allfit.presentation.search.FavoritedSearchPane
import allfit.presentation.search.RatingSearchPane
import allfit.presentation.search.SearchRequest
import allfit.presentation.search.SearchView
import allfit.presentation.search.TextSearchPane
import allfit.presentation.search.WishlistedSearchPane
import tornadofx.FXEvent
import tornadofx.hbox
import tornadofx.vbox

class PartnersSearchView : SearchView<FullPartner>(SearchRequest.alwaysTrue()) {

    override val root = hbox(spacing = 30.0) {
        vbox(spacing = 5.0) {
            addIt(TextSearchPane(::checkSearch))
            addIt(CheckinSearchPane(::checkSearch))
            addIt(RatingSearchPane(::checkSearch))
        }
        vbox(spacing = 5.0) {
            addIt(FavoritedSearchPane(::checkSearch))
            addIt(WishlistedSearchPane(::checkSearch))
        }
    }

    override fun buildEvent(request: SearchRequest<FullPartner>): FXEvent =
        PartnerSearchFXEvent(request)
}