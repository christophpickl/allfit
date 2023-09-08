package allfit.presentation.search

import allfit.presentation.Styles
import allfit.presentation.models.FullWorkout
import javafx.scene.control.ToggleGroup
import tornadofx.addClass
import tornadofx.radiobutton
import tornadofx.selectedValueProperty
import tornadofx.singleAssign
import tornadofx.togglegroup

enum class VisitedState {
    UPCOMING,
    VISITED,
    BOTH,
}

data class VisitedSearchRequest(
    val visisted: VisitedState,
) : SubSearchRequest<FullWorkout> {
    override val predicate: (FullWorkout) -> Boolean = { workout ->
        when (visisted) {
            VisitedState.UPCOMING -> !workout.wasVisited
            VisitedState.VISITED -> workout.wasVisited
            VisitedState.BOTH -> true
        }
    }
}

class VisitedSearchPane(checkSearch: () -> Unit) : SearchPane<FullWorkout>() {

    private var visitedGroup by singleAssign<ToggleGroup>()

    override var searchFieldPane: SearchFieldPane = searchField(alwaysEnabled = true) {
        visitedGroup = togglegroup {
            selectedValueProperty<VisitedState>().addListener { _, _, _ ->
                checkSearch()
            }
            radiobutton("Upcoming", value = VisitedState.UPCOMING) {
                isSelected = true
                addClass(Styles.linkInternal)
            }
            radiobutton("Visited", value = VisitedState.VISITED) {
                addClass(Styles.linkInternal)
            }
            radiobutton("Both", value = VisitedState.BOTH) {
                addClass(Styles.linkInternal)
            }
        }
    }

    override fun buildSearchRequest() = VisitedSearchRequest(
        visisted = visitedGroup.selectedValueProperty<VisitedState>().value
    )
}
