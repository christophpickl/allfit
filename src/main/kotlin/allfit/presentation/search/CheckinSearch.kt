package allfit.presentation.search

import allfit.presentation.models.FullWorkout
import javafx.scene.control.ComboBox
import tornadofx.combobox
import tornadofx.selectedItem
import tornadofx.singleAssign

data class CheckinSearchRequest(
    val operand: Int,
    val operator: NumericOperator,
) : SubSearchRequest {
    override val predicate: (FullWorkout) -> Boolean = { workout ->
        operator.comparator(workout.partner.checkins, operand)
    }
}

class CheckinSearchPane(checkSearch: () -> Unit) : SearchPane() {

    private val checkinOptions = listOf(0, 1, 5, 10, 20)
    private var checkinsOperator: ComboBox<String> by singleAssign()
    private var checkinsOperand: ComboBox<Int> by singleAssign()

    override var searchFieldPane: SearchFieldPane = searchField {
        title = "Checkins"
        enabledAction = OnEnabledAction { checkSearch() }
        checkinsOperator = combobox(values = NumericOperator.values().map { it.symbol }) {
            selectionModel.select(0)
            setOnAction { checkSearch() }
        }
        checkinsOperand = combobox(values = checkinOptions) {
            selectionModel.select(0)
            setOnAction { checkSearch() }
        }
    }

    override fun buildSearchRequest() = CheckinSearchRequest(
        operand = checkinsOperand.selectedItem!!,
        operator = NumericOperator.bySymbol(checkinsOperator.selectedItem!!),
    )
}
