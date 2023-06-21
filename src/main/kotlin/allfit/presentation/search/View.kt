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

abstract class SearchPane<T> : View() {

    abstract var searchFieldPane: SearchFieldPane
    override val root get() = searchFieldPane

    protected abstract fun buildSearchRequest(): SubSearchRequest<T>?

    fun maybeBuildSearchRequest(): SubSearchRequest<T>? =
        if (searchFieldPane.isEnabled) buildSearchRequest() else null
}

class OnEnabledAction(val listener: () -> Unit)

class SearchFieldPane(private val alwaysEnabled: Boolean) : HBox() {

    var title: String by property("")
    private fun titleProperty() = getProperty(SearchFieldPane::title)

    private var enabledCheckbox: CheckBox by singleAssign()
    val isEnabled get() = if (alwaysEnabled) true else enabledCheckbox.isSelected
    var enabledAction: OnEnabledAction by property(OnEnabledAction({}))

    init {
        if (!alwaysEnabled) {
            enabledCheckbox = checkbox {
                action {
                    enabledAction.listener()
                }
            }
        }
        label {
            bind(titleProperty())
        }
    }
}

fun EventTarget.searchField(alwaysEnabled: Boolean = false, op: SearchFieldPane.() -> Unit = {}): SearchFieldPane {
    val searchField = SearchFieldPane(alwaysEnabled)
    return opcr(this, searchField, op)
}
