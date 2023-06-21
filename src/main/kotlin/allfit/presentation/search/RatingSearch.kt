package allfit.presentation.search

import allfit.presentation.renderStars
import javafx.scene.control.ComboBox
import tornadofx.combobox
import tornadofx.selectedItem
import tornadofx.singleAssign

interface HasRating {
    val rating: Int
}

data class RatingSearchRequest<T : HasRating>(
    val searchRating: Int,
    val operator: NumericOperator,
) : SubSearchRequest<T> {
    override val predicate: (T) -> Boolean = {
        operator.comparator(it.rating, searchRating)
    }
}

class RatingSearchPane<T : HasRating>(checkSearch: () -> Unit) : SearchPane<T>() {

    private val ratingOptions = listOf(0, 1, 2, 3, 4, 5)
    private var ratingsOperator: ComboBox<String> by singleAssign()
    private var ratingOperand: ComboBox<Int> by singleAssign()

    override var searchFieldPane: SearchFieldPane = searchField {
        title = "Rating"
        enabledAction = OnEnabledAction { checkSearch() }
        ratingsOperator = combobox(values = NumericOperator.values().map { it.symbol }) {
            selectionModel.select(0)
            setOnAction { checkSearch() }
        }
        ratingOperand = combobox(values = ratingOptions) {
            cellFormat {
                text = it.renderStars()
            }
            selectionModel.select(0)
            setOnAction { checkSearch() }
        }
    }


    override fun buildSearchRequest() = RatingSearchRequest<T>(
        searchRating = ratingOperand.selectedItem!!,
        operator = NumericOperator.bySymbol(ratingsOperator.selectedItem!!),
    )
}
