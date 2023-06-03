package allfit.presentation.partnersview

import allfit.presentation.Styles
import allfit.presentation.models.PartnersViewModel
import javafx.scene.layout.Priority
import tornadofx.App
import tornadofx.View
import tornadofx.borderpane
import tornadofx.center
import tornadofx.launch
import tornadofx.vgrow

class PartnersWindowApp : App(
    primaryView = PartnersWindow::class,
    stylesheet = Styles::class,
)

class PartnersWindow : View() {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            launch<PartnersWindowApp>()
        }
    }

    private val partnersViewModel: PartnersViewModel by inject()
    private val partnersTable = PartnersTable()

    init {
        title = "Partners"
        partnersViewModel.allPartners.bindTo(partnersTable)
        partnersTable.applySort()
    }

    override val root = borderpane {
//        top {
//            vgrow = Priority.NEVER
//            add(label("Filter"))
//        }
        center {
            vgrow = Priority.ALWAYS
            add(partnersTable)
        }
    }
}

