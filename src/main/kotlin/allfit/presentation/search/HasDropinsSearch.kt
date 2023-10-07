package allfit.presentation.search

import allfit.presentation.components.TrileanBox
import allfit.presentation.components.trileanbox
import allfit.presentation.models.FullPartner
import allfit.presentation.models.Trilean
import tornadofx.singleAssign

data class HasDropinsSearchRequest(
    val operand: Trilean,
) : SubSearchRequest<FullPartner> {
    override val predicate: (FullPartner) -> Boolean = {
        it.hasDropins == operand
    }
}

class HasDropinsSearchPane(checkSearch: () -> Unit) : SearchPane<FullPartner>() {
    private var operand: TrileanBox by singleAssign()
    override var searchFieldPane = searchField {
        title = "Has Dropins"
        enabledAction = OnEnabledAction { checkSearch() }
        operand = trileanbox(checkSearch)
    }

    override fun buildSearchRequest() = HasDropinsSearchRequest(operand = operand.value)
}
