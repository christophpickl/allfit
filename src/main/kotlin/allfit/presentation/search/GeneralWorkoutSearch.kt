package allfit.presentation.search

import allfit.presentation.Styles
import allfit.presentation.models.DateRange
import allfit.presentation.models.FullWorkout
import javafx.scene.control.ToggleGroup
import tornadofx.addClass
import tornadofx.radiobutton
import tornadofx.selectedValueProperty
import tornadofx.singleAssign
import tornadofx.togglegroup

enum class GeneralWorkoutFilter {
    UPCOMING,
    VISITED,
    ANY,
    THIS_PERIOD,
}

data class GeneralWorkoutSearchRequest(
    val visisted: GeneralWorkoutFilter,
    val period: DateRange,
) : SubSearchRequest<FullWorkout> {
    override val predicate: (FullWorkout) -> Boolean = { workout ->
        when (visisted) {
            GeneralWorkoutFilter.UPCOMING -> !workout.wasVisited
            GeneralWorkoutFilter.VISITED -> workout.wasVisited
            GeneralWorkoutFilter.ANY -> true
            GeneralWorkoutFilter.THIS_PERIOD -> {
                (workout.date.start in period) && (workout.wasVisited || workout.isReserved)
            }
        }
    }
}

class GeneralWorkoutSearchPane(
    private val period: DateRange,
    checkSearch: () -> Unit,
) : SearchPane<FullWorkout>() {

    private var workoutFilterValues by singleAssign<ToggleGroup>()

    override var searchFieldPane: SearchFieldPane = searchField(alwaysEnabled = true) {
        workoutFilterValues = togglegroup {
            selectedValueProperty<GeneralWorkoutFilter>().addListener { _, _, _ ->
                checkSearch()
            }
            radiobutton("Upcoming", value = GeneralWorkoutFilter.UPCOMING) {
                isSelected = true
                addClass(Styles.linkInternal)
            }
            radiobutton("This Period", value = GeneralWorkoutFilter.THIS_PERIOD) {
                addClass(Styles.linkInternal)
            }
            radiobutton("Visited", value = GeneralWorkoutFilter.VISITED) {
                addClass(Styles.linkInternal)
            }
            radiobutton("Any", value = GeneralWorkoutFilter.ANY) {
                addClass(Styles.linkInternal)
            }
        }
    }

    override fun buildSearchRequest() = GeneralWorkoutSearchRequest(
        visisted = workoutFilterValues.selectedValueProperty<GeneralWorkoutFilter>().value,
        period = period,
    )
}
