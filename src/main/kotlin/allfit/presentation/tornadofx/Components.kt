package allfit.presentation.tornadofx

import allfit.presentation.Styles
import allfit.presentation.logic.openBrowser
import javafx.beans.Observable
import javafx.beans.binding.StringBinding
import javafx.beans.value.ObservableValue
import javafx.event.EventTarget
import javafx.scene.Cursor
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.Tooltip
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import tornadofx.action
import tornadofx.addClass
import tornadofx.bind
import tornadofx.button
import tornadofx.enableWhen
import tornadofx.hbox
import tornadofx.label
import tornadofx.stringBinding
import tornadofx.tooltip

fun Pane.labelDetailMultibind(
    prompt: String,
    vararg dependencies: Observable,
    smallSize: Boolean = false,
    textColor: Color? = null,
    textMaxWidth: Double? = null,
    link: ObservableValue<String>? = null,
    transformer: () -> String,
) {
    internalLlabelDetail(prompt, MultiLabelValue(dependencies, transformer), smallSize, textColor, textMaxWidth, link)
}

fun Pane.labelDetail(
    prompt: String,
    value: ObservableValue<String>,
    smallSize: Boolean = false,
    textColor: Color? = null,
    textMaxWidth: Double? = null,
    link: ObservableValue<String>? = null,
) {
    internalLlabelDetail(prompt, SingleLabelValue(value), smallSize, textColor, textMaxWidth, link)
}

private interface LabelValue {
    fun bindLabel(label: Label)
    fun bindTooltip(tooltip: Tooltip)
}

private class SingleLabelValue(
    private val value: ObservableValue<String>
) : LabelValue {
    override fun bindLabel(label: Label) {
        label.text = value.value
        label.bind(value)
    }

    override fun bindTooltip(tooltip: Tooltip) {
        tooltip.text = value.value
        value.addListener { _, _, newValue ->
            tooltip.text = newValue
        }
    }
}

private class MultiLabelValue(
    private val dependencies: Array<out Observable>, private val transformer: () -> String
) : LabelValue {

    private lateinit var binding: StringBinding

    override fun bindLabel(label: Label) {
        binding = stringBinding(label, dependencies = dependencies, op = { transformer() })
        label.text = binding.get()
    }

    override fun bindTooltip(tooltip: Tooltip) {
        tooltip.text = binding.get()
        binding.addListener { _, _, newValue ->
            tooltip.text = newValue
        }
    }
}

private fun Pane.internalLlabelDetail(
    prompt: String,
    valueParam: LabelValue,
    smallSize: Boolean = false,
    textColor: Color? = null,
    textMaxWidth: Double? = null,
    link: ObservableValue<String>? = null,
) {
    hbox {
        labelPrompt(prompt, smallSize)
        label {
            valueParam.bindLabel(this)
            tooltip {
                // TODO only show if width is > maxWidth and text is cut off
                valueParam.bindTooltip(this)
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
                styleAsLink(link.value)
            }
        }
    }
}

fun Label.styleAsLink(url: String) {
    textFill = Color.BLUE
    isUnderline = true
    cursor = Cursor.HAND
    setOnMouseClicked {
        openBrowser(url)
    }
}

fun EventTarget.link(url: String, op: Label.() -> Unit = {}): Label =
    label {
        text = url
        styleAsLink(url)
        op()
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
