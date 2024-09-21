package allfit.presentation.search

import allfit.presentation.models.FullPartner
import allfit.presentation.workouts.WorkoutsViewModel
import javafx.scene.control.ComboBox
import tornadofx.combobox
import tornadofx.selectedItem
import tornadofx.singleAssign

data class CategorySearchRequest(
    val searchCategory: String,
) : SubSearchRequest<FullPartner> {
    override val predicate: (FullPartner) -> Boolean = { partner ->
        partner.categories.contains(searchCategory)
    }
}

class CategorySearchPane(checkSearch: () -> Unit) : SearchPane<FullPartner>() {

    private val mainViewModel: WorkoutsViewModel by inject()
    private var selectedCategory: ComboBox<String> by singleAssign()

    override var searchFieldPane = searchField {
        title = "Category"
        enabledAction = OnEnabledAction { checkSearch() }
        selectedCategory = combobox(values = mainViewModel.allCategories) {
            setOnAction {
                checkSearch()
            }
        }
    }

    override fun buildSearchRequest() =
        selectedCategory.selectedItem?.let { CategorySearchRequest(it) }

}
