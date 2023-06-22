package allfit.presentation.search

import allfit.presentation.logic.StaticIcon
import allfit.presentation.logic.StaticIconStorage
import allfit.presentation.models.FullWorkout
import allfit.presentation.view.ImageToggleEffect
import allfit.presentation.view.imagedToggleButton
import javafx.scene.control.ToggleButton
import tornadofx.singleAssign

data class WishlistedSearchRequest(
    val operand: Boolean,
) : SubSearchRequest<FullWorkout> {
    override val predicate: (FullWorkout) -> Boolean = { workout ->
        workout.partner.isWishlisted == operand
    }
}

class WishlistedSearchPane(checkSearch: () -> Unit) : SearchPane<FullWorkout>() {

    private var wishlistedOperand: ToggleButton by singleAssign()

    override var searchFieldPane = searchField {
        title = "Wishlisted"
        enabledAction = OnEnabledAction { checkSearch() }
        wishlistedOperand = imagedToggleButton(
            effect = ImageToggleEffect.Saturation,
            imageTrue = StaticIconStorage.get(StaticIcon.WishlistFull),
            imageFalse = StaticIconStorage.get(StaticIcon.WishlistOutline),
        ) {
            selectedProperty().addListener { _ ->
                checkSearch()
            }
        }
    }

    override fun buildSearchRequest() =
        WishlistedSearchRequest(operand = wishlistedOperand.isSelected)
}
