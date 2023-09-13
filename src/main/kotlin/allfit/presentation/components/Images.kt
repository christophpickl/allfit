package allfit.presentation.components

import allfit.presentation.tornadofx.setAllHeights
import allfit.presentation.tornadofx.setAllWidths
import javafx.beans.value.ObservableValue
import javafx.event.EventTarget
import javafx.scene.image.Image
import javafx.scene.layout.Background
import tornadofx.imageview
import tornadofx.pane

private const val size = 200.0

fun EventTarget.bigImage(image: ObservableValue<Image?>, background: Background? = null) {
    pane {
        if (background != null) {
            this.background = background
        }
        setAllHeights(size)
        setAllWidths(size)
        imageview(image) {
            image.addListener { _, _, newImg ->
                val (newW, newH) = adjustMaxSize(newImg!!.width, newImg.height)
                fitWidthProperty().set(newW)
                fitHeightProperty().set(newH)
            }
        }
    }
}

private fun adjustMaxSize(w: Double, h: Double): Pair<Double, Double> =
    if (w == h) {
        size to size
    } else if (w > h) {
        val ratio = h / w
        size to (size * ratio)
    } else { // h > w
        val ratio = w / h
        (size * ratio) to size
    }
