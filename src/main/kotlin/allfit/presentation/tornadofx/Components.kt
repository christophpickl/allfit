package allfit.presentation.tornadofx

import allfit.presentation.Styles
import javafx.beans.value.ObservableValue
import javafx.event.EventTarget
import javafx.scene.layout.Pane
import tornadofx.*
import java.awt.Desktop
import java.net.URI

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

fun EventTarget.openWebsiteButton(url: ObservableValue<String>) {
    button("Open Website") {
        tooltip {
            this@tooltip.textProperty().bind(url)
        }
        action {
            Desktop.getDesktop().browse(URI(url.value))
        }
    }
}
