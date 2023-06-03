package allfit.presentation.tornadofx

import javafx.scene.layout.Region
import javafx.scene.web.WebView

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
