package allfit.presentation.preferences

import allfit.domain.Location
import allfit.persistence.domain.SinglesRepo
import javafx.geometry.Pos
import javafx.scene.control.ComboBox
import tornadofx.View
import tornadofx.action
import tornadofx.button
import tornadofx.combobox
import tornadofx.hbox
import tornadofx.label
import tornadofx.paddingAll
import tornadofx.singleAssign
import tornadofx.vbox

class PreferencesView : View() {

    private var locationBox by singleAssign<ComboBox<Location>>()
    private val singlesRepo: SinglesRepo by di()

    override val root = vbox(spacing = 10.0) {
        paddingAll = 5.0
        hbox(spacing = 3.0, alignment = Pos.CENTER_LEFT) {
            label("Location:")
            locationBox = combobox(values = Location.entries) {
                cellFormat {
                    text = it.label
                }
            }
        }
        locationBox.selectionModel.select(singlesRepo.selectLocation())
        button("Save") {
            action {
                singlesRepo.updateLocation(locationBox.selectionModel.selectedItem)
            }
        }
        label("Restart the application after saving so changes will have an effect.")
    }
}
