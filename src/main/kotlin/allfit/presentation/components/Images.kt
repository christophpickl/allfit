package allfit.presentation.components

import allfit.presentation.tornadofx.setAllHeights
import allfit.presentation.tornadofx.setAllWidths
import allfit.presentation.view.ViewConstants
import javafx.beans.value.ObservableValue
import javafx.event.EventTarget
import javafx.scene.image.Image
import tornadofx.imageview
import tornadofx.scrollpane

fun EventTarget.bigImage(image: ObservableValue<Image?>) {
    scrollpane(fitToHeight = true) {
        setAllHeights(ViewConstants.bigImageHeight)
        setAllWidths(ViewConstants.bigImageHeight)
        imageview(image)
    }
}
