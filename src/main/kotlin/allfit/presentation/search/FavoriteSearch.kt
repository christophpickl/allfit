package allfit.presentation.search

import allfit.presentation.models.FullWorkout
import javafx.scene.control.CheckBox
import tornadofx.action
import tornadofx.checkbox
import tornadofx.singleAssign

data class FavoriteSearchRequest(
    val operand: Boolean,
) : SubSearchRequest<FullWorkout> {
    override val predicate: (FullWorkout) -> Boolean = { workout ->
        workout.partner.isFavorited == operand
    }
}

class FavoriteSearchPane(checkSearch: () -> Unit) : SearchPane<FullWorkout>() {

    private var favoriteOperand: CheckBox by singleAssign()

    override var searchFieldPane = searchField {
        title = "Favorited"
        enabledAction = OnEnabledAction { checkSearch() }
        favoriteOperand = checkbox {
            action {
                checkSearch()
            }
        }
    }


    override fun buildSearchRequest() = FavoriteSearchRequest(operand = favoriteOperand.isSelected)
}
