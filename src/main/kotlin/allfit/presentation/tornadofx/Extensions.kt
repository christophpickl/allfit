package allfit.presentation.tornadofx

import java.awt.Toolkit
import javafx.scene.layout.Region
import javafx.scene.web.WebView
import tornadofx.Component

fun WebView.setAllHeights(heights: Double) {
    maxHeight = heights
    minHeight = heights
    prefHeight = heights
}

fun Region.setAllHeights(heights: Double) {
    maxHeight = heights
    minHeight = heights
    prefHeight = heights
}

fun Region.setAllWidths(widths: Double) {
    maxWidth = widths
    minWidth = widths
    prefWidth = widths
}

fun Component.maximizeWindow() {
    val screenSize = Toolkit.getDefaultToolkit().screenSize
    primaryStage.x = 0.0
    primaryStage.y = 0.0
    primaryStage.width = screenSize.width.toDouble()
    primaryStage.height = screenSize.height.toDouble()
}
