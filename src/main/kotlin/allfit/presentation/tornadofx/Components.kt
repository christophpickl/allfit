package allfit.presentation.tornadofx

import allfit.presentation.Styles
import java.awt.Desktop
import java.net.URI
import javafx.beans.value.ObservableValue
import javafx.event.EventTarget
import javafx.scene.control.Button
import javafx.scene.layout.Pane
import tornadofx.action
import tornadofx.addClass
import tornadofx.bind
import tornadofx.button
import tornadofx.enableWhen
import tornadofx.hbox
import tornadofx.label
import tornadofx.tooltip

fun Pane.labelDetail(prompt: String, value: ObservableValue<String>) {
    hbox {
        labelPrompt(prompt)
        label().bind(value)
    }
}

fun Pane.labelPrompt(prompt: String) {
    label("$prompt: ") {
        addClass(Styles.detailPrompt)
    }
}

fun EventTarget.openWebsiteButton(url: ObservableValue<String>, op: Button.() -> Unit = {}) {
    button("Open Website") {
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
