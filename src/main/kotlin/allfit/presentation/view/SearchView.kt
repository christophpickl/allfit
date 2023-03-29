package allfit.presentation.view

import allfit.presentation.SearchFXEvent
import allfit.presentation.logic.CheckinSearchRequest
import allfit.presentation.logic.DateSearchRequest
import allfit.presentation.logic.FavoriteSearchRequest
import allfit.presentation.logic.SearchRequest
import allfit.presentation.logic.SubSearchRequest
import allfit.service.toZonedDate
import javafx.event.EventTarget
import javafx.scene.control.CheckBox
import javafx.scene.control.ComboBox
import javafx.scene.control.DatePicker
import javafx.scene.layout.HBox
import tornadofx.View
import tornadofx.action
import tornadofx.bind
import tornadofx.checkbox
import tornadofx.combobox
import tornadofx.datepicker
import tornadofx.getProperty
import tornadofx.label
import tornadofx.opcr
import tornadofx.property
import tornadofx.selectedItem
import tornadofx.singleAssign
import tornadofx.vbox

enum class NumericOperator(val symbol: String, val comparator: (Int, Int) -> Boolean) {
    Equals("=", { x, y -> x == y }),
    NotEquals("!=", { x, y -> x != y }),
    GreaterEquals(">=", { x, y -> x >= y }),
    LowerEquals("<=", { x, y -> x <= y });

    companion object {
        fun bySymbol(search: String) = NumericOperator.values().firstOrNull { it.symbol == search }
            ?: error("Invalid numeric operator symbol: '$search'")
    }
}


class SearchView : View() {

    private var previousSearchRequest = SearchRequest.empty

    private val searches: List<SearchPane> = listOf(
        CheckinsSearch(::checkSearch),
        FavoriteSearch(::checkSearch),
        DateSearch(::checkSearch),
    )

    override val root = vbox {
        searches.forEach {
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
        searches.mapNotNull { it.maybeBuildSearchRequest() }.toSet()
    )
}

fun EventTarget.searchField(op: SearchFieldPane.() -> Unit = {}): SearchFieldPane {
    val searchField = SearchFieldPane()
    return opcr(this, searchField, op)
}


class OnEnabledAction(val listener: () -> Unit)

class SearchFieldPane : HBox() {
    private var enabledCheckbox: CheckBox by singleAssign()
    val isEnabled get() = enabledCheckbox.isSelected
    var title: String by property("")
    private fun titleProperty() = getProperty(SearchFieldPane::title)
    var enabledAction: OnEnabledAction by property(OnEnabledAction({}))

    init {
        enabledCheckbox = checkbox {
            action {
                enabledAction.listener()
            }
        }
        label {
            bind(titleProperty())
        }
    }
}

abstract class SearchPane : HBox() {

    abstract var searchFieldPane: SearchFieldPane

    protected abstract fun buildSearchRequest(): SubSearchRequest?

    fun maybeBuildSearchRequest(): SubSearchRequest? =
        if (searchFieldPane.isEnabled) buildSearchRequest() else null
}

class CheckinsSearch(checkSearch: () -> Unit) : SearchPane() {
    override var searchFieldPane: SearchFieldPane by singleAssign()
    private var checkinsOperator: ComboBox<String> by singleAssign()
    private var checkinsOperand: ComboBox<Int> by singleAssign()

    init {
        searchFieldPane = searchField {
            title = "Checkins"
            enabledAction = OnEnabledAction { checkSearch() }
            checkinsOperator = combobox(values = NumericOperator.values().map { it.symbol }) {
                selectionModel.select(0)
                setOnAction { checkSearch() }
            }
            checkinsOperand = combobox(values = listOf(0, 1, 5, 10, 20)) {
                selectionModel.select(0)
                setOnAction { checkSearch() }
            }
        }
    }

    override fun buildSearchRequest() = CheckinSearchRequest(
        operand = checkinsOperand.selectedItem!!,
        operator = NumericOperator.bySymbol(checkinsOperator.selectedItem!!),
    )
}

class FavoriteSearch(checkSearch: () -> Unit) : SearchPane() {
    override var searchFieldPane: SearchFieldPane by singleAssign()
    private var favoriteOperand: CheckBox by singleAssign()

    init {
        searchFieldPane = searchField {
            title = "Favorited"
            enabledAction = OnEnabledAction { checkSearch() }
            favoriteOperand = checkbox {
                action {
                    checkSearch()
                }
            }
        }
    }

    override fun buildSearchRequest() = FavoriteSearchRequest(operand = favoriteOperand.isSelected)
}

class DateSearch(checkSearch: () -> Unit) : SearchPane() {
    override var searchFieldPane: SearchFieldPane by singleAssign()
    var dateInput: DatePicker by singleAssign()

    init {
        searchFieldPane = searchField {
            title = "Date"
            enabledAction = OnEnabledAction { checkSearch() }
            dateInput = datepicker {
                prefWidth = 120.0
                isShowWeekNumbers = false
                setOnAction {
                    checkSearch()
                }
            }
        }
    }

    override fun buildSearchRequest() = dateInput.value?.let {
        DateSearchRequest(operand = it.toZonedDate())
    }
}
