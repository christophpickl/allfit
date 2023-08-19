package allfit.presentation.components

import allfit.presentation.tornadofx.setAllHeights
import allfit.presentation.tornadofx.setAllWidths
import allfit.presentation.view.ViewConstants
import javafx.beans.value.ObservableValue
import javafx.event.EventTarget
import javafx.scene.image.Image
import tornadofx.borderpane
import tornadofx.center
import tornadofx.imageview

fun EventTarget.bigImage(image: ObservableValue<Image?>) {
//    scrollpane {
    borderpane {
        setAllHeights(ViewConstants.BIG_IMAGE_HEIGHT)
        setAllWidths(ViewConstants.BIG_IMAGE_HEIGHT)
        center {
            imageview(image)
        }

    }
//    }
}
