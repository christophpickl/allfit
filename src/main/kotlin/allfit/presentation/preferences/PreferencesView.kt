package allfit.presentation.preferences

import allfit.domain.Location
import allfit.persistence.domain.SinglesRepo
import javafx.scene.control.ComboBox
import tornadofx.View
import tornadofx.action
import tornadofx.button
import tornadofx.combobox
import tornadofx.label
import tornadofx.singleAssign
import tornadofx.vbox

class PreferencesView : View() {

    private var locationBox by singleAssign<ComboBox<Location>>()
    private val singlesRepo: SinglesRepo by di()

    override val root = vbox {
        label("Location:")
        locationBox = combobox(values = Location.entries) {
            cellFormat {
                text = it.label
            }
        }
        locationBox.selectionModel.select(singlesRepo.selectLocation())
        button("Save") {
            action {
                singlesRepo.updateLocation(locationBox.selectionModel.selectedItem)
            }
        }
    }
}
