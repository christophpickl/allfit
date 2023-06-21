package allfit.presentation.partnersview

import allfit.presentation.HidePartnerFXEvent
import allfit.presentation.PresentationConstants
import allfit.presentation.UnhidePartnerFXEvent
import allfit.presentation.logic.StaticImage
import allfit.presentation.logic.StaticImageStorage
import allfit.presentation.models.FullPartner
import allfit.presentation.models.Usage
import allfit.presentation.tornadofx.applyInitSort
import allfit.presentation.tornadofx.imageColumn
import allfit.service.Clock
import allfit.service.toPrettyString
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.control.SelectionMode
import javafx.scene.control.TableColumn
import javafx.scene.control.TableView
import tornadofx.FX
import tornadofx.action
import tornadofx.column
import tornadofx.contextmenu
import tornadofx.fixedWidth
import tornadofx.item
import tornadofx.remainingWidth
import tornadofx.selectedItem
import tornadofx.smartResize
import tornadofx.weightedWidth

class PartnersTable(
    usage: Usage,
    clock: Clock,
) : TableView<FullPartner>() {

    private val nameColumn: TableColumn<FullPartner, String>
    private val hiddenImage = StaticImageStorage.get(StaticImage.Hidden)

    init {
        smartResize()
        selectionModel.selectionMode = SelectionMode.SINGLE

        imageColumn(maxWidth = PresentationConstants.tableImageWidth) { it.value.imageProperty() }

        nameColumn = column<FullPartner, String>("Name") { it.value.nameProperty() }.remainingWidth().weightedWidth(0.5)

        column<FullPartner, Int>("Chk") { it.value.checkinsProperty() }.fixedWidth(40)
        column<FullPartner, Int>("Wrk") { SimpleObjectProperty(it.value.upcomingWorkouts.size) }.fixedWidth(40)
        column<FullPartner, Int>("Avail") {
            SimpleObjectProperty(it.value.availability(usage))
        }.fixedWidth(40)

        column("Last") { SimpleObjectProperty(it.value.lastCheckin?.toPrettyString(clock)) }.fixedWidth(110)

        imageColumn(maxWidth = 30.0) {
            SimpleObjectProperty(if (it.value.isHidden) hiddenImage else null)
        }

        contextmenu {
            item("Toggle hidden status").action {
                selectedItem?.also { partner ->
                    FX.eventbus.fire(
                        if (partner.isHidden) UnhidePartnerFXEvent(partner.id)
                        else HidePartnerFXEvent(partner.id)
                    )
                }
            }
        }
    }

    // has to be invoked after init
    fun applySort() {
        applyInitSort(nameColumn)
    }
}
