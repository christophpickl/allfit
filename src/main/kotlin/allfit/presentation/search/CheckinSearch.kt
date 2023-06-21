package allfit.presentation.search

import javafx.scene.control.ComboBox
import tornadofx.combobox
import tornadofx.selectedItem
import tornadofx.singleAssign

interface HasCheckins {
    val checkins: Int
}

data class CheckinSearchRequest<T : HasCheckins>(
    val operand: Int,
    val operator: NumericOperator,
) : SubSearchRequest<T> {
    override val predicate: (T) -> Boolean = {
        operator.comparator(it.checkins, operand)
//        operator.comparator(workout.partner.checkins, operand)
    }
}

class CheckinSearchPane<T : HasCheckins>(checkSearch: () -> Unit) : SearchPane<T>() {

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

    override fun buildSearchRequest() = CheckinSearchRequest<T>(
        operand = checkinsOperand.selectedItem!!,
        operator = NumericOperator.bySymbol(checkinsOperator.selectedItem!!),
    )
}
