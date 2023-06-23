package allfit.presentation.tornadofx

import allfit.presentation.Styles
import java.awt.Desktop
import java.net.URI
import javafx.beans.value.ObservableValue
import javafx.event.EventTarget
import javafx.scene.control.Button
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import tornadofx.action
import tornadofx.addClass
import tornadofx.bind
import tornadofx.button
import tornadofx.enableWhen
import tornadofx.hbox
import tornadofx.label
import tornadofx.tooltip

fun Pane.labelDetail(
    prompt: String,
    value: ObservableValue<String>,
    smallSize: Boolean = false,
    textColor: Color? = null,
    textMaxWidth: Double? = null,
) {
    hbox {
        labelPrompt(prompt, smallSize)
        label {
            bind(value)
            tooltip {
                value.addListener { _, _, newValue ->
                    text = newValue
                }
            }
            textMaxWidth?.let {
                maxWidth = it
            }
            if (smallSize) {
                addClass(Styles.small)
            }
            textColor?.let {
                textFill = it
            }
        }
    }
}

fun Pane.labelPrompt(prompt: String, smallSize: Boolean = false) {
    label("$prompt: ") {
        addClass(Styles.detailPrompt)
        if (smallSize) {
            addClass(Styles.small)
        }
    }
}

fun EventTarget.openWebsiteButton(url: ObservableValue<String>, op: Button.() -> Unit = {}) {
    button("Website") {
        tooltip {
            this@tooltip.textProperty().bind(url)
        }
        action {
            Desktop.getDesktop().browse(URI(url.value))
        }
        enableWhen {
            url.map { it.isNotEmpty() }
        }
        op()
    }
}
