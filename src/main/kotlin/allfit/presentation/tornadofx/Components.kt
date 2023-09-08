package allfit.presentation.tornadofx

import allfit.presentation.Styles
import allfit.presentation.logic.openBrowser
import javafx.beans.Observable
import javafx.beans.binding.StringBinding
import javafx.beans.value.ObservableValue
import javafx.event.EventTarget
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
    linkIsExternal: Boolean = false,
    transformer: () -> String,
) {
    internalLabelDetail(
        prompt,
        MultiLabelValue(dependencies, transformer),
        smallSize,
        textColor,
        textMaxWidth,
        link,
        linkIsExternal
    )
}

fun Pane.labelDetail(
    prompt: String,
    value: ObservableValue<String>,
    smallSize: Boolean = false,
    textColor: Color? = null,
    textMaxWidth: Double? = null,
    link: ObservableValue<String>? = null,
    isExternal: Boolean = false,
) {
    internalLabelDetail(prompt, SingleLabelValue(value), smallSize, textColor, textMaxWidth, link, isExternal)
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

private fun Pane.internalLabelDetail(
    prompt: String,
    valueParam: LabelValue,
    smallSize: Boolean = false,
    textColor: Color? = null,
    textMaxWidth: Double? = null,
    link: ObservableValue<String>? = null,
    linkIsExternal: Boolean,
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
                addClass(if (linkIsExternal) Styles.linkExternal else Styles.linkInternal)
                openBrowserOnClick(link)
            }
        }
    }
}

fun Label.openBrowserOnClick(url: ObservableValue<String>) {
    setOnMouseClicked {
        openBrowser(url.value)
    }
}

fun Label.openBrowserOnClick(url: String) {
    setOnMouseClicked {
        openBrowser(url)
    }
}

fun EventTarget.link(url: String, linkIsExternal: Boolean = false, op: Label.() -> Unit = {}): Label =
    label {
        text = url
        addClass(if (linkIsExternal) Styles.linkExternal else Styles.linkInternal)
        openBrowserOnClick(url)
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
    button("Open Website") {
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
