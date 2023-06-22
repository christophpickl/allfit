package allfit.presentation.view

import javafx.scene.Node
import javafx.scene.effect.ColorAdjust

fun Node.effectRemoveColors(enabled: Boolean = true, removeSaturation: Double = -1.0) {
    effect = ColorAdjust().apply {
        saturation = if (enabled) removeSaturation else 0.0
    }
}

fun Node.effectIncreaseBrightness(enabled: Boolean = true) {
    effect = ColorAdjust().apply {
        brightness = if (enabled) 0.0 else 0.7
    }
}
