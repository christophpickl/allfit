package allfit.presentation.search

import allfit.presentation.models.FullWorkout
import javafx.scene.control.CheckBox
import tornadofx.action
import tornadofx.checkbox
import tornadofx.singleAssign

data class IsWorkoutReservedSearchRequest(
    val operand: Boolean,
) : SubSearchRequest<FullWorkout> {
    override val predicate: (FullWorkout) -> Boolean = { workout ->
        workout.isReserved == operand
    }
}

class IsWorkoutReservedSearchPane(checkSearch: () -> Unit) : SearchPane<FullWorkout>() {

    var reservedCheckBox: CheckBox by singleAssign()

    override var searchFieldPane = searchField {
        title = "Reserved"
        enabledAction = OnEnabledAction { checkSearch() }
        reservedCheckBox = checkbox {
            action {
                checkSearch()
            }
        }
    }

    override fun buildSearchRequest() =
        IsWorkoutReservedSearchRequest(operand = reservedCheckBox.isSelected)

}
