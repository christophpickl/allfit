package allfit.presentation

import javafx.scene.layout.BorderStrokeStyle
import javafx.scene.paint.Color
import javafx.scene.shape.StrokeLineCap
import javafx.scene.shape.StrokeLineJoin
import javafx.scene.shape.StrokeType
import javafx.scene.text.FontWeight
import tornadofx.*

class Styles : Stylesheet() {

    companion object {
        val header1 by cssclass()
        val detailPrompt by cssclass()
        val htmlview by cssclass()

        val blue = c("#477ADB")
    }

    init {
        Stylesheet.root {
            backgroundColor += Color.WHITE
            padding = box(10.px)
        }
        label {
            fontSize = 14.px
        }
        header1 {
            fontSize = 28.px
            fontWeight = FontWeight.EXTRA_BOLD
            textFill = blue
//            backgroundColor += blue
        }
        detailPrompt {
            // TODO bold doesnt work?! fontWeight = FontWeight.EXTRA_BOLD
            textFill = Color.GRAY
        }
        htmlview {
            borderColor += box(Color.DARKGRAY)
            borderStyle += BorderStrokeStyle(
                StrokeType.INSIDE,
                StrokeLineJoin.MITER,
                StrokeLineCap.BUTT,
                10.0,
                0.0,
                listOf(25.0, 5.0)
            )
            borderWidth += box(5.px)
        }
//        button, checkBox, Stylesheet.comboBox {
//            backgroundColor += blue
//        }
        // button:hover

//        textArea {
//            content {
//                +flat
//                +backgroundHoverColors
//            }
//        }
    }
}