package allfit.presentation.search

import allfit.presentation.components.ImageToggleEffect
import allfit.presentation.components.imagedToggleButton
import allfit.presentation.logic.StaticIcon
import allfit.presentation.logic.StaticIconStorage
import allfit.presentation.models.FullWorkout
import javafx.scene.control.ToggleButton
import tornadofx.singleAssign

data class FavoritedSearchRequest(
    val operand: Boolean,
) : SubSearchRequest<FullWorkout> {
    override val predicate: (FullWorkout) -> Boolean = { workout ->
        workout.partner.isFavorited == operand
    }
}

class FavoritedSearchPane(checkSearch: () -> Unit) : SearchPane<FullWorkout>() {

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


    override fun buildSearchRequest() = FavoritedSearchRequest(operand = favoritedOperand.isSelected)
}
