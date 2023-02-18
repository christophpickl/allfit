package allfit

import javafx.scene.paint.Color
import tornadofx.*

class RootView : View() {
    private val inpEmail = textfield()
    private val inpPassword = textfield()
    private val controller = Controller()

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
