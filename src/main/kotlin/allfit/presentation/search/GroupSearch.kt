package allfit.presentation.search

import allfit.presentation.models.FullWorkout
import allfit.presentation.models.MainViewModel
import javafx.scene.control.ComboBox
import tornadofx.combobox
import tornadofx.selectedItem
import tornadofx.singleAssign

data class GroupSearchRequest(
    val searchGroup: String,
) : SubSearchRequest {
    override val predicate: (FullWorkout) -> Boolean = { workout ->
        workout.partner.categories.contains(searchGroup)
    }
}

class GroupSearchPane(checkSearch: () -> Unit) : SearchPane() {

    private val mainViewModel: MainViewModel by inject()
    private var selectedGroup: ComboBox<String> by singleAssign()

    override var searchFieldPane = searchField {
        title = "Group"
        enabledAction = OnEnabledAction { checkSearch() }
        selectedGroup = combobox(values = mainViewModel.allGroups) {
            setOnAction {
                checkSearch()
            }
        }
    }

    override fun buildSearchRequest() =
        selectedGroup.selectedItem?.let { GroupSearchRequest(it) }

}
