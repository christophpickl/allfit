package allfit.presentation.search

import javafx.event.EventTarget
import javafx.scene.control.CheckBox
import javafx.scene.layout.HBox
import tornadofx.View
import tornadofx.action
import tornadofx.bind
import tornadofx.checkbox
import tornadofx.getProperty
import tornadofx.label
import tornadofx.opcr
import tornadofx.property
import tornadofx.singleAssign

abstract class SearchPane : View() {

    companion object {
        fun buildAll(checkSearch: () -> Unit): List<SearchPane> = listOf(
            DateSearchPane(checkSearch),
            GroupSearchPane(checkSearch),
            CheckinSearchPane(checkSearch),
            WishlistedSearchPane(checkSearch),
            FavoriteSearchPane(checkSearch),
            ReservedSearch(checkSearch),
        )
    }

    abstract var searchFieldPane: SearchFieldPane
    override val root get() = searchFieldPane

    protected abstract fun buildSearchRequest(): SubSearchRequest?

    fun maybeBuildSearchRequest(): SubSearchRequest? =
        if (searchFieldPane.isEnabled) buildSearchRequest() else null
}

class OnEnabledAction(val listener: () -> Unit)

class SearchFieldPane : HBox() {
    private var enabledCheckbox: CheckBox by singleAssign()
    val isEnabled get() = enabledCheckbox.isSelected
    var title: String by property("")
    private fun titleProperty() = getProperty(SearchFieldPane::title)
    var enabledAction: OnEnabledAction by property(OnEnabledAction({}))

    init {
        enabledCheckbox = checkbox {
            action {
                enabledAction.listener()
            }
        }
        label {
            bind(titleProperty())
        }
    }
}

fun EventTarget.searchField(op: SearchFieldPane.() -> Unit = {}): SearchFieldPane {
    val searchField = SearchFieldPane()
    return opcr(this, searchField, op)
}
