package allfit.presentation.search

import allfit.presentation.logic.StaticIcon
import allfit.presentation.logic.StaticIconStorage
import allfit.presentation.models.FullWorkout
import allfit.presentation.view.ImageToggleEffect
import allfit.presentation.view.imagedToggleButton
import javafx.scene.control.ToggleButton
import tornadofx.singleAssign

data class ReservedSearchRequest(
    val operand: Boolean,
) : SubSearchRequest<FullWorkout> {
    override val predicate: (FullWorkout) -> Boolean = { workout ->
        workout.isReserved == operand
    }
}

class ReservedSearchPane(checkSearch: () -> Unit) : SearchPane<FullWorkout>() {

    private var reservedCheckBox by singleAssign<ToggleButton>()

    override var searchFieldPane = searchField {
        title = "Reserved"
        enabledAction = OnEnabledAction { checkSearch() }
        val reservedImage = StaticIconStorage.get(StaticIcon.Reserved)
        val reservedImage2 = StaticIconStorage.get(StaticIcon.ReservedNot)
        reservedCheckBox = imagedToggleButton(
            effect = ImageToggleEffect.Brightness,
            imageTrue = reservedImage,
            imageFalse = reservedImage2,
        ) {
            selectedProperty().addListener { _ ->
                checkSearch()
            }
        }
    }

    override fun buildSearchRequest() =
        ReservedSearchRequest(operand = reservedCheckBox.isSelected)

}
