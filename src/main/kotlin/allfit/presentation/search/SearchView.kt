package allfit.presentation.search

import javafx.event.EventTarget
import tornadofx.FXEvent
import tornadofx.View

abstract class SearchView<T>(
    private val alwaysIncludeSearchRequest: SubSearchRequest<T>,
) : View() {

    private var previousSearchRequest: SearchRequest<T> = SearchRequest.empty()
    private val allSearchPanes = mutableListOf<SearchPane<T>>()

    abstract fun buildEvent(request: SearchRequest<T>): FXEvent

    protected fun EventTarget.addIt(pane: SearchPane<T>) {
        add(pane)
        allSearchPanes += pane
    }

    private fun buildSearchRequest() = SearchRequest(
        allSearchPanes.mapNotNull { it.maybeBuildSearchRequest() }.toSet(),
        alwaysIncludeSearchRequest
    )

    fun checkSearch() {
        val currentSearchRequest = buildSearchRequest()
        if (previousSearchRequest == currentSearchRequest) {
            return
        }
        previousSearchRequest = currentSearchRequest
        fire(buildEvent(currentSearchRequest))
    }
}
