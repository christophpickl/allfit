package allfit.presentation

import javafx.scene.Cursor
import javafx.scene.paint.Color
import javafx.scene.paint.CycleMethod
import javafx.scene.paint.LinearGradient
import javafx.scene.paint.Stop
import javafx.scene.text.FontWeight
import tornadofx.Stylesheet
import tornadofx.box
import tornadofx.c
import tornadofx.cssclass
import tornadofx.px

class Styles : Stylesheet() {

    companion object {
        val header1 by cssclass()
        val detailPrompt by cssclass()
        val htmlview by cssclass()
        val small by cssclass()
        val linkInternal by cssclass()
        val linkExternal by cssclass()

        val standardBlue = c("#477ADB")
        val clickLinkInternalColor = c("#899AC1")
        val clickLinkInternalHoverColor = c("#9DB0DC")
        val clickLinkExternalColor = c("#193EFF")
        val clickLinkExternalHoverColor = c("#4B67FF")
    }

    init {
        Stylesheet.root {
            backgroundColor += Color.WHITE
            padding = box(0.px)
        }
        label {
            fontSize = 14.px
        }
        small {
            fontSize = 10.px
        }
        header1 {
            fontSize = 28.px
            fontWeight = FontWeight.EXTRA_BOLD
            textFill = Color.BLACK
        }
        tooltip {
            backgroundColor += LinearGradient(
                0.0, 0.0, 0.0, 10.0, true, CycleMethod.NO_CYCLE,
                Stop(0.0, Color.WHITE), Stop(10.0, standardBlue)
            )
            textFill = Color.BLACK
            fontSize = 12.px
        }
        detailPrompt {
            // TODO bold doesnt work?! fontWeight = FontWeight.EXTRA_BOLD
            textFill = Color.GRAY
        }
        s(linkInternal, linkExternal) {
            underline = true
            cursor = Cursor.HAND
        }
        linkInternal {
            textFill = clickLinkInternalColor
            and(hover) {
                textFill = clickLinkInternalHoverColor
            }
        }
        linkExternal {
            textFill = clickLinkExternalColor
            and(hover) {
                textFill = clickLinkExternalHoverColor
            }
        }

//        textArea {
//            content {
//                +flat
//                +backgroundHoverColors
//            }
//        }
    }
}