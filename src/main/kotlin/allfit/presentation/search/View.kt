package allfit.presentation.search

import javafx.event.EventTarget
import javafx.scene.control.CheckBox
import javafx.scene.layout.HBox
import tornadofx.action
import tornadofx.bind
import tornadofx.checkbox
import tornadofx.getProperty
import tornadofx.label
import tornadofx.opcr
import tornadofx.property
import tornadofx.singleAssign

abstract class SearchPane : HBox() {

    companion object {
        fun buildAll(checkSearch: () -> Unit): List<SearchPane> = listOf(
            CheckinSearchPane(checkSearch),
            FavoriteSearchPane(checkSearch),
            DateSearchPane(checkSearch),
            ReservedSearch(checkSearch),
        )
    }

    abstract var searchFieldPane: SearchFieldPane

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
