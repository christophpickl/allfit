package allfit.presentation.search

import allfit.domain.IsFavoritable
import allfit.presentation.components.favoriteToggleButton
import javafx.scene.control.ToggleButton
import tornadofx.singleAssign

data class FavoritedSearchRequest<T : IsFavoritable>(
    val operand: Boolean,
) : SubSearchRequest<T> {
    override val predicate: (T) -> Boolean = {
        it.isFavorited == operand
    }
}

class FavoritedSearchPane<T : IsFavoritable>(checkSearch: () -> Unit) : SearchPane<T>() {

    private var favoritedOperand: ToggleButton by singleAssign()

    override var searchFieldPane = searchField {
        title = "Favorited"
        enabledAction = OnEnabledAction { checkSearch() }
        favoritedOperand = favoriteToggleButton {
            isSelected = true
            selectedProperty().addListener { _ ->
                checkSearch()
            }
        }
    }

    override fun buildSearchRequest() = FavoritedSearchRequest<T>(operand = favoritedOperand.isSelected)
}
