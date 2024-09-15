package allfit.presentation.partners

import allfit.presentation.PartnerSearchFXEvent
import allfit.presentation.models.FullPartner
import allfit.presentation.search.*
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
        vbox(spacing = 5.0) {
            addIt(HasDropinsSearchPane(::checkSearch))
            addIt(HasWorkoutsSearchPane(::checkSearch))
            addIt(IsHiddenSearchPane(::checkSearch))
        }
    }

    override fun buildEvent(request: SearchRequest<FullPartner>): FXEvent =
        PartnerSearchFXEvent(request)
}