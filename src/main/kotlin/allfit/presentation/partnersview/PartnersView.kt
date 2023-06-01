package allfit.presentation.partnersview

import allfit.presentation.models.FullPartner
import allfit.presentation.models.PartnersViewModel
import allfit.presentation.tornadofx.applyInitSort
import javafx.scene.control.SelectionMode
import javafx.scene.control.TableColumn
import javafx.scene.control.TableView
import javafx.scene.layout.Priority
import tornadofx.View
import tornadofx.borderpane
import tornadofx.center
import tornadofx.column
import tornadofx.fixedWidth
import tornadofx.label
import tornadofx.remainingWidth
import tornadofx.smartResize
import tornadofx.top
import tornadofx.vgrow
import tornadofx.weightedWidth

class PartnersView : View() {

    private val partnersViewModel: PartnersViewModel by inject()
    private val partnersTable = PartnersTable()

    init {
        title = "Partners"
        partnersViewModel.allPartners.bindTo(partnersTable)
        partnersTable.applySort()
    }

    override val root = borderpane {
        top {
            vgrow = Priority.NEVER
            add(label("Filter"))
        }
        center {
            vgrow = Priority.ALWAYS
            add(partnersTable)
        }
    }
}

class PartnersTable() : TableView<FullPartner>() {

    private val nameColumn: TableColumn<FullPartner, String>

    init {
        smartResize()
        selectionModel.selectionMode = SelectionMode.SINGLE

        nameColumn = column<FullPartner, String>("Name") { it.value.nameProperty() }
            .remainingWidth()
            .weightedWidth(0.5)

        column<FullPartner, Int>("#") { it.value.checkinsProperty() }
            .fixedWidth(30)
    }

    // has to be invoked after init
    fun applySort() {
        applyInitSort(nameColumn)
    }
}
