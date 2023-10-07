package allfit.presentation.search

import allfit.presentation.components.TrileanBox
import allfit.presentation.components.trileanbox
import allfit.presentation.models.FullPartner
import allfit.presentation.models.Trilean
import tornadofx.singleAssign

data class HasWorkoutsSearchRequest(
    val operand: Trilean,
) : SubSearchRequest<FullPartner> {
    override val predicate: (FullPartner) -> Boolean = {
        it.hasWorkouts == operand
    }
}

class HasWorkoutsSearchPane(checkSearch: () -> Unit) : SearchPane<FullPartner>() {
    private var operand: TrileanBox by singleAssign()
    override var searchFieldPane = searchField {
        title = "Has Workouts"
        enabledAction = OnEnabledAction { checkSearch() }
        operand = trileanbox(checkSearch)
    }

    override fun buildSearchRequest() = HasWorkoutsSearchRequest(operand = operand.value)
}
