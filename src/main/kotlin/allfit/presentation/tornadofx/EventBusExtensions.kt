package allfit.presentation.tornadofx

import allfit.presentation.ErrorDialog
import tornadofx.Component
import tornadofx.FXEvent

inline fun <reified E : FXEvent> Component.safeSubscribe(crossinline function: () -> Boolean) {
    subscribe<E> {
        try {
            function()
        } catch (e: Exception) {
            ErrorDialog.show(e)
        }
    }
}
