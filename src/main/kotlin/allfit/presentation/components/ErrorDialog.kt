package allfit.presentation.components

import javafx.scene.layout.Priority
import mu.KotlinLogging.logger
import tornadofx.View
import tornadofx.textarea
import tornadofx.vbox
import tornadofx.vgrow

class ErrorDialog(private val throwable: Throwable) : View() {
    companion object {
        private val log = logger {}

        fun show(throwable: Throwable) {
            log.error(throwable) { "Uncaught exception detected!" }
            val dialog = ErrorDialog(throwable)
            dialog.dialog("Fatal error!") {
                vbox {
                    vgrow = Priority.ALWAYS
                    textarea(throwable.stackTraceToString())
                }
//            button("Dismiss") {
//                action {
//                    dialog.close()
//                }
//            }
            }
        }
    }

    override val root = vbox {

        // nope, this has no effect
//        textarea(throwable.stackTraceToString())
    }
}