package allfit.presentation.view

import allfit.presentation.SearchFXEvent
import allfit.presentation.search.SearchPane
import allfit.presentation.search.SearchRequest
import tornadofx.View
import tornadofx.vbox

class SearchView : View() {

    private var previousSearchRequest = SearchRequest.empty
    private val allSearchPanes = SearchPane.buildAll(::checkSearch)

    override val root = vbox {
        allSearchPanes.forEach {
            add(it)
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
