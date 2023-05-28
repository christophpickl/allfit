package allfit.presentation.view

import allfit.presentation.SearchFXEvent
import allfit.presentation.search.DateSearchPane
import allfit.presentation.search.FavoriteSearchPane
import allfit.presentation.search.GroupSearchPane
import allfit.presentation.search.IsWishlistedSearchPane
import allfit.presentation.search.IsWorkoutReservedSearchPane
import allfit.presentation.search.PartnerCheckinSearchPane
import allfit.presentation.search.SearchPane
import allfit.presentation.search.SearchRequest
import allfit.presentation.search.TextSearchPane
import allfit.service.Clock
import javafx.event.EventTarget
import tornadofx.View
import tornadofx.hbox
import tornadofx.vbox

class SearchView : View() {

    private var previousSearchRequest = SearchRequest.empty
    private val clock: Clock by di()
    private val allSearchPanes = mutableListOf<SearchPane>()

    private fun EventTarget.addIt(pane: SearchPane) {
        add(pane)
        allSearchPanes += pane
    }

    override val root = hbox {
        vbox {
            addIt(TextSearchPane(::checkSearch))
            addIt(DateSearchPane(clock, ::checkSearch))
            addIt(GroupSearchPane(::checkSearch))
            addIt(PartnerCheckinSearchPane(::checkSearch))
        }
        vbox {
            addIt(IsWishlistedSearchPane(::checkSearch))
            addIt(FavoriteSearchPane(::checkSearch))
            addIt(IsWorkoutReservedSearchPane(::checkSearch))
        }
    }

    private fun checkSearch() {
        val currentSearchRequest = buildSearchRequest()
        if (previousSearchRequest == currentSearchRequest) {
            return
        }
        previousSearchRequest = currentSearchRequest
        fire(SearchFXEvent(currentSearchRequest))
    }

    private fun buildSearchRequest() = SearchRequest(
        allSearchPanes.mapNotNull { it.maybeBuildSearchRequest() }.toSet()
    )
}
