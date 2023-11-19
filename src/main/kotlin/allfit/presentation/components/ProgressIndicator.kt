package allfit.presentation.components

import javafx.beans.value.ObservableValue
import javafx.scene.Group
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle
import tornadofx.Fragment
import tornadofx.group
import tornadofx.rectangle
import tornadofx.stackpane

private const val lineHeight = 3.0
private val leftoverLineColor = Color.GRAY

class DualProgressIndicator(
    private val lineColor1: Color,
    private val lineColor2: Color,
    private val percentage1: ObservableValue<Double>,
    private val percentage2: ObservableValue<Double>,
) : Fragment() {

    private var maxLineWidth: Double = 0.0
    private lateinit var rect1: Rectangle
    private lateinit var rect2: Rectangle
    private lateinit var rect3: Rectangle

    override val root = stackpane {
        group {
            rect1 = rect(lineHeight, lineColor1)
            rect2 = rect(lineHeight, lineColor2)
            rect3 = rect(lineHeight, leftoverLineColor)
            percentage1.addListener { _, _, _ ->
                readjustSize()
            }
            percentage2.addListener { _, _, _ ->
                readjustSize()
            }
        }
    }

    fun setMaxLineWidth(newValue: Double) {
        maxLineWidth = newValue
        readjustSize()
    }

    private fun readjustSize() {
        rect1.width = maxLineWidth * percentage1.orElse(0.0).value
        rect2.x = rect1.width
        rect2.width = maxLineWidth * percentage2.orElse(0.0).value

        val rect12width = rect1.width + rect2.width
        rect3.x = rect12width
        rect3.width = maxLineWidth - rect12width
    }

}


class ProgressIndicator(
    private val lineColor: Color,
    private val percentage: ObservableValue<Double>,
) : Fragment() {

    private var maxLineWidth: Double = 0.0
    private lateinit var rect1: Rectangle
    private lateinit var rect2: Rectangle

    override val root = stackpane {
        group {
            rect1 = rect(lineHeight, lineColor)
            rect2 = rect(lineHeight, leftoverLineColor)
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

private fun Group.rect(lineHeight: Double, lineColor: Color) =
    rectangle {
        x = 0.0
        y = 0.0
        height = lineHeight
        fill = lineColor
    }
