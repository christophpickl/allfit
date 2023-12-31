package allfit.presentation.components

import io.github.oshai.kotlinlogging.KotlinLogging.logger
import javafx.scene.layout.Priority
import tornadofx.View
import tornadofx.textarea
import tornadofx.vbox
import tornadofx.vgrow

class ErrorDialog : View() {
    
    companion object {

        private val log = logger {}

        fun show(throwable: Throwable) {
            log.error(throwable) { "Uncaught exception detected!" }
            val dialog = ErrorDialog()
            dialog.dialog("Fatal error!") {
                vgrow = Priority.ALWAYS
                vbox {
                    vgrow = Priority.ALWAYS
                    textarea(throwable.stackTraceToString()) {
                        vgrow = Priority.ALWAYS
                    }
                }
            }
        }
    }

    override val root = vbox()
}