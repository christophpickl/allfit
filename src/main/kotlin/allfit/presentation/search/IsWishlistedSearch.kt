package allfit.presentation.search

import allfit.presentation.models.FullWorkout
import javafx.scene.control.CheckBox
import tornadofx.action
import tornadofx.checkbox
import tornadofx.singleAssign

data class IsWishlistedSearchRequest(
    val operand: Boolean,
) : SubSearchRequest<FullWorkout> {
    override val predicate: (FullWorkout) -> Boolean = { workout ->
        workout.partner.isWishlisted == operand
    }
}

class IsWishlistedSearchPane(checkSearch: () -> Unit) : SearchPane<FullWorkout>() {

    private var wishlistedOperand: CheckBox by singleAssign()

    override var searchFieldPane = searchField {
        title = "Wishlisted"
        enabledAction = OnEnabledAction { checkSearch() }
        wishlistedOperand = checkbox {
            action {
                checkSearch()
            }
        }
    }

    override fun buildSearchRequest() = IsWishlistedSearchRequest(operand = wishlistedOperand.isSelected)
}
