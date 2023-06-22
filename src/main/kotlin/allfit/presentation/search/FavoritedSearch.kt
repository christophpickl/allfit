package allfit.presentation.search

import allfit.presentation.logic.StaticIcon
import allfit.presentation.logic.StaticIconStorage
import allfit.presentation.models.FullWorkout
import allfit.presentation.view.ImageToggleEffect
import allfit.presentation.view.imagedToggleButton
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
            selectedProperty().addListener { _ ->
                checkSearch()
            }
        }
    }


    override fun buildSearchRequest() = FavoritedSearchRequest(operand = favoritedOperand.isSelected)
}
