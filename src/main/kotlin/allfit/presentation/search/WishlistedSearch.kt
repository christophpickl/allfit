package allfit.presentation.search

import allfit.presentation.models.FullWorkout
import javafx.scene.control.CheckBox
import tornadofx.action
import tornadofx.checkbox
import tornadofx.singleAssign

data class WishlistedSearchRequest(
    val operand: Boolean,
) : SubSearchRequest {
    override val predicate: (FullWorkout) -> Boolean = { workout ->
        workout.partner.isWishlisted == operand
    }
}

class WishlistedSearchPane(checkSearch: () -> Unit) : SearchPane() {

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

    override fun buildSearchRequest() = WishlistedSearchRequest(operand = wishlistedOperand.isSelected)
}
