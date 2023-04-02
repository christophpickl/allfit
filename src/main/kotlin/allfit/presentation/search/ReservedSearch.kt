package allfit.presentation.search

import allfit.presentation.models.FullWorkout
import javafx.scene.control.CheckBox
import tornadofx.action
import tornadofx.checkbox
import tornadofx.singleAssign

data class ReservedSearchRequest(
    val operand: Boolean,
) : SubSearchRequest {
    override val predicate: (FullWorkout) -> Boolean = { workout ->
        workout.isReserved == operand
    }
}

class ReservedSearch(checkSearch: () -> Unit) : SearchPane() {

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
        ReservedSearchRequest(operand = reservedCheckBox.isSelected)

}
