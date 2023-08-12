package allfit.presentation.search

import allfit.presentation.components.ImageToggleEffect
import allfit.presentation.components.imagedToggleButton
import allfit.presentation.logic.StaticIcon
import allfit.presentation.logic.StaticIconStorage
import javafx.scene.control.ToggleButton
import tornadofx.singleAssign

interface IsFavoritable {
    val isFavorited: Boolean
}

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
        favoritedOperand = imagedToggleButton(
            effect = ImageToggleEffect.Saturation,
            imageTrue = StaticIconStorage.get(StaticIcon.FavoriteFull),
            imageFalse = StaticIconStorage.get(StaticIcon.FavoriteOutline),
        ) {
            isSelected = true
            selectedProperty().addListener { _ ->
                checkSearch()
            }
        }
    }


    override fun buildSearchRequest() = FavoritedSearchRequest<T>(operand = favoritedOperand.isSelected)
}
