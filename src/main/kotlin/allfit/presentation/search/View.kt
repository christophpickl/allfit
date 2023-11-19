package allfit.presentation.search

import allfit.presentation.Styles
import javafx.event.EventTarget
import javafx.geometry.Pos
import javafx.scene.control.CheckBox
import javafx.scene.layout.HBox
import tornadofx.View
import tornadofx.addClass
import tornadofx.bind
import tornadofx.checkbox
import tornadofx.getProperty
import tornadofx.label
import tornadofx.opcr
import tornadofx.property
import tornadofx.singleAssign
import tornadofx.toProperty

abstract class SearchPane<T> : View() {

    abstract var searchFieldPane: SearchFieldPane
    override val root get() = searchFieldPane

    protected abstract fun buildSearchRequest(): SubSearchRequest<T>?

    fun maybeBuildSearchRequest(): SubSearchRequest<T>? =
        if (searchFieldPane.isEnabled) buildSearchRequest() else null
}

class OnEnabledAction(val listener: () -> Unit)

class SearchFieldPane(private val alwaysEnabled: Boolean) : HBox(4.0) {

    var title: String by property("")
    private fun titleProperty() = getProperty(SearchFieldPane::title)

    private var enabledCheckbox: CheckBox by singleAssign()
    val isEnabled get() = if (alwaysEnabled) true else isEnabledProperty.get()
    var enabledAction: OnEnabledAction by property(OnEnabledAction {})

    private val isEnabledProperty = false.toProperty().also {
        it.addListener { _ ->
            enabledAction.listener()
        }
    }

    init {
        alignment = Pos.CENTER_LEFT
        if (!alwaysEnabled) {
            enabledCheckbox = checkbox(property = isEnabledProperty)
        }
        label {
            bind(titleProperty())
            addClass(Styles.linkInternal)
            setOnMouseClicked {
                isEnabledProperty.set(!isEnabledProperty.get())
            }
        }
    }
}

fun EventTarget.searchField(alwaysEnabled: Boolean = false, op: SearchFieldPane.() -> Unit = {}): SearchFieldPane {
    val searchField = SearchFieldPane(alwaysEnabled)
    return opcr(this, searchField, op)
}
