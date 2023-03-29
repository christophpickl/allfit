package allfit.presentation

import javafx.scene.paint.Color
import javafx.scene.text.FontWeight
import tornadofx.Stylesheet
import tornadofx.c
import tornadofx.cssclass
import tornadofx.px

class Styles : Stylesheet() {

    companion object {
        val header1 by cssclass()

        val blue = c("#477ADB")
    }

    init {
        Stylesheet.root {
            backgroundColor += Color.WHITE
            padding = tornadofx.box(10.px)
        }
        header1 {
            fontSize = 18.px
            fontWeight = FontWeight.EXTRA_BOLD
        }
//        button {
//            backgroundColor += blue
//        }
//        textArea {
//            content {
//                +flat
//                +backgroundHoverColors
//            }
//        }
    }
}