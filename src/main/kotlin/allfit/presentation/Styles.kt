package allfit.presentation

import javafx.scene.paint.Color
import tornadofx.Stylesheet
import tornadofx.px

class Styles : Stylesheet() {
    init {
        Stylesheet.root {
            backgroundColor += Color.WHITE
            padding = tornadofx.box(10.px)
        }
    }
}