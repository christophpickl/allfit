package allfit.presentation.tornadofx

import allfit.presentation.Styles
import allfit.presentation.logic.openBrowser
import javafx.beans.value.ObservableValue
import javafx.event.EventTarget
import javafx.scene.Cursor
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
    link: ObservableValue<String>? = null,
) {
    hbox {
        labelPrompt(prompt, smallSize)
        label {
            bind(value)
            tooltip {
                // TODO only show if width is > maxWidth and text is cut off
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
            if (link != null) {
                textFill = Color.BLUE
                isUnderline = true
                cursor = Cursor.HAND
                setOnMouseClicked {
                    openBrowser(link.value)
                }
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
            openBrowser(url.value)
        }
        enableWhen {
            url.map { it.isNotEmpty() }
        }
        op()
    }
}
