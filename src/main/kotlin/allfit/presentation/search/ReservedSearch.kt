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

    override var searchFieldPane: SearchFieldPane by singleAssign()
    var reservedCheckBox: CheckBox by singleAssign()

    init {
        searchFieldPane = searchField {
            title = "Reserved"
            enabledAction = OnEnabledAction { checkSearch() }
            reservedCheckBox = checkbox {
                action {
                    checkSearch()
                }
            }
        }
    }

    override fun buildSearchRequest() =
        ReservedSearchRequest(operand = reservedCheckBox.isSelected)

}
