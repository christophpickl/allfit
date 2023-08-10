package allfit.presentation.search

import allfit.presentation.components.ImageToggleEffect
import allfit.presentation.components.imagedToggleButton
import allfit.presentation.logic.StaticIcon
import allfit.presentation.logic.StaticIconStorage
import allfit.presentation.models.FullWorkout
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
            isSelected = true
            selectedProperty().addListener { _ ->
                checkSearch()
            }
        }
    }

    override fun buildSearchRequest() = ReservedSearchRequest(operand = reservedCheckBox.isSelected)

}
