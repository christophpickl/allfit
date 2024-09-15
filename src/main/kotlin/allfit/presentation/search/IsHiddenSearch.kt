package allfit.presentation.search

import allfit.presentation.components.TrileanBox
import allfit.presentation.components.trileanbox
import allfit.presentation.logic.StaticIcon
import allfit.presentation.logic.StaticIconStorage
import allfit.presentation.models.FullPartner
import javafx.scene.control.CheckBox
import tornadofx.checkbox
import tornadofx.imageview
import tornadofx.singleAssign

data class IsHiddenSearchRequest(
    val operand: Boolean,
) : SubSearchRequest<FullPartner> {
    override val predicate: (FullPartner) -> Boolean = {
        it.isHidden == operand
    }
}

class IsHiddenSearchPane(checkSearch: () -> Unit) : SearchPane<FullPartner>() {
    private var operand: CheckBox by singleAssign()
    override var searchFieldPane = searchField {
        title = "Is Hidden"
        enabledAction = OnEnabledAction { checkSearch() }
        imageview(StaticIconStorage.get(StaticIcon.Hidden))
        operand = checkbox {
            setOnAction {
                checkSearch()
            }
        }
    }

    override fun buildSearchRequest() = IsHiddenSearchRequest(operand = operand.isSelected)
}
