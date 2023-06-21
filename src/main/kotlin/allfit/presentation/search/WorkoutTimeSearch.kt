package allfit.presentation.search

import allfit.presentation.models.FullWorkout
import allfit.service.Clock
import java.time.ZonedDateTime
import javafx.scene.control.ToggleGroup
import tornadofx.radiobutton
import tornadofx.selectedValueProperty
import tornadofx.singleAssign
import tornadofx.togglegroup

enum class WorkoutTime {
    UPCOMING,
    VISITED,
    BOTH,
}

data class WorkoutTimeSearchRequest(
    val now: ZonedDateTime,
    val time: WorkoutTime,
) : SubSearchRequest<FullWorkout> {
    override val predicate: (FullWorkout) -> Boolean = { workout ->
        when (time) {
            WorkoutTime.UPCOMING -> workout.date.start >= now
            WorkoutTime.VISITED -> workout.wasVisited
            WorkoutTime.BOTH -> true
        }
    }
}

class WorkoutTimeSearchPane(private val clock: Clock, checkSearch: () -> Unit) : SearchPane<FullWorkout>() {

    private var workoutTimeGroup by singleAssign<ToggleGroup>()

    override var searchFieldPane: SearchFieldPane = searchField(alwaysEnabled = true) {
        workoutTimeGroup = togglegroup {
            selectedValueProperty<WorkoutTime>().addListener { _, _, _ ->
                checkSearch()
            }
            radiobutton("Upcoming", value = WorkoutTime.UPCOMING) {
                isSelected = true
            }
            radiobutton("Visited", value = WorkoutTime.VISITED)
            radiobutton("Both", value = WorkoutTime.BOTH)
        }
    }

    override fun buildSearchRequest() = WorkoutTimeSearchRequest(
        now = clock.now(),
        time = workoutTimeGroup.selectedValueProperty<WorkoutTime>().value
    )
}
