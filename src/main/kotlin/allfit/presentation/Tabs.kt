package allfit.presentation

import javafx.scene.Node
import javafx.scene.input.KeyEvent
import tornadofx.Component
import tornadofx.FXEvent

// TODO this is good enough, yet the key listener could be even more global (e.g. when in textfield "stuck")

fun Node.setOnTabShortcutListener(parent: Component) {
    setOnKeyTyped {
        it.checkForSelectTabShortcut()?.also { parent.fire(it) }
    }
}

fun KeyEvent.checkForSelectTabShortcut(): SelectTabFXEvent? =
    if (isMetaDown && (character == "1" || character == "2" || character == "3")) {
        val tabNumber = character.toInt() - 1
        SelectTabFXEvent(SelectTabFXEvent.byNumber(tabNumber))
    } else null

class SelectTabFXEvent(val tab: TabIndex) : FXEvent() {
    companion object {
        fun byNumber(number: Int): TabIndex =
            TabIndex.byNumber[number] ?: error("invalid tab number: $number")
    }
}

enum class TabIndex(val number: Int) {
    Workouts(0), Partners(1), Notes(2);

    companion object {
        val byNumber by lazy {
            values().associateBy { it.number }
        }
    }
}
