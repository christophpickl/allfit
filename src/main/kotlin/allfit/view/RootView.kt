package allfit.view

import allfit.Environment
import javafx.scene.paint.Color
import tornadofx.Stylesheet
import tornadofx.View
import tornadofx.action
import tornadofx.box
import tornadofx.button
import tornadofx.hbox
import tornadofx.label
import tornadofx.plusAssign
import tornadofx.px
import tornadofx.textfield
import tornadofx.vbox

class RootView : View() {

    private val controller by inject<MainController>()

    private val search = textfield()

    init {
        title = "AllFit " + (if (Environment.current == Environment.Development) " - DEV" else "")
    }

    override val root = vbox {
        hbox {
            this += search
            button("Search") {
                action {
                    controller.search(search.text)
                }
            }
        }
        val categories = controller.loadCategories()
        label("Categories: ${categories.categories.joinToString(", ") { it.name }}")
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
