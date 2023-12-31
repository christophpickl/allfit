package allfit.presentation.components

import allfit.presentation.view.effectIncreaseBrightness
import allfit.presentation.view.effectRemoveColors
import javafx.beans.binding.Bindings
import javafx.event.EventTarget
import javafx.scene.control.ToggleButton
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.Background
import tornadofx.opcr

enum class ImageToggleEffect {
    Saturation {
        override fun applyOn(isHovering: Boolean, toggle: ToggleButton) {
            toggle.effectRemoveColors(!isHovering, OUT_HOVER_SATURATION)
        }

        override fun initOn(toggle: ToggleButton) {
            toggle.effectRemoveColors(true, OUT_HOVER_SATURATION)
        }
    },
    Brightness {
        override fun applyOn(isHovering: Boolean, toggle: ToggleButton) {
            toggle.effectIncreaseBrightness(isHovering)
        }

        override fun initOn(toggle: ToggleButton) {
            toggle.effectIncreaseBrightness(false)
        }
    };

    companion object {
        private const val OUT_HOVER_SATURATION = -0.6
    }

    abstract fun applyOn(isHovering: Boolean, toggle: ToggleButton)
    abstract fun initOn(toggle: ToggleButton)
}

fun EventTarget.imagedToggleButton(
    effect: ImageToggleEffect,
    imageTrue: Image,
    imageFalse: Image,
    op: ToggleButton.() -> Unit = {}
): ToggleButton {
    val toggle = ToggleButton()
    toggle.hoverProperty().addListener { _, _, isHovering: Boolean ->
        effect.applyOn(isHovering, toggle)
    }
    effect.initOn(toggle)
    toggle.background = Background.EMPTY

    val toggleImage = ImageView()
    toggle.graphic = toggleImage
    toggleImage.imageProperty().bind(
        Bindings
            .`when`(toggle.selectedProperty())
            .then(imageTrue)
            .otherwise(imageFalse)
    )
    return opcr(this, toggle, op)
}
