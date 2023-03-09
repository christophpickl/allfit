package allfit.view

import javafx.scene.paint.Color
import tornadofx.Stylesheet
import tornadofx.View
import tornadofx.action
import tornadofx.box
import tornadofx.button
import tornadofx.plusAssign
import tornadofx.px
import tornadofx.textfield
import tornadofx.vbox

class RootView : View() {

    private val controller by inject<MainController>()

    private val inpEmail = textfield()
    private val inpPassword = textfield()

    init {
        title = "AllFit"
    }

    override val root = vbox {
        this += inpEmail
        this += inpPassword
        button("Login") {
            action {
                controller.login(inpEmail.text, inpPassword.text)
            }
        }
    }
}

class Styles : Stylesheet() {
    init {
        Stylesheet.root {
            backgroundColor += Color.WHITE
            padding = box(10.px)
        }
    }
}
