package allfit.presentation.components

import javafx.beans.value.ObservableValue
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle
import tornadofx.Fragment
import tornadofx.group
import tornadofx.rectangle
import tornadofx.stackpane

class ProgressIndicator(
    private val lineColor: Color,
    private val percentage: ObservableValue<Double>,
) : Fragment() {

    private val lineHeight = 3.0
    private var maxLineWidth: Double = 0.0
    private lateinit var rect1: Rectangle
    private lateinit var rect2: Rectangle

    override val root = stackpane {
        group {
            rect1 = rectangle {
                x = 0.0
                y = 0.0
                height = lineHeight
                fill = lineColor
            }
            rect2 = rectangle() {
                y = 0.0
                height = lineHeight
                fill = Color.GRAY
            }
            percentage.addListener { _, _, _ ->
                readjustSize()
            }
        }
    }

    fun setMaxLineWidth(newValue: Double) {
        maxLineWidth = newValue
        readjustSize()
    }

    private fun readjustSize() {
        rect1.width = maxLineWidth * percentage.orElse(0.0).value
        rect2.x = rect1.width
        rect2.width = maxLineWidth - rect1.width
    }
}