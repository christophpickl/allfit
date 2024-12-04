package allfit.presentation.preferences

import allfit.domain.Location
import javafx.geometry.Pos
import javafx.scene.control.ComboBox
import tornadofx.View
import tornadofx.action
import tornadofx.bindSelected
import tornadofx.button
import tornadofx.combobox
import tornadofx.hbox
import tornadofx.label
import tornadofx.paddingAll
import tornadofx.singleAssign
import tornadofx.vbox

class PreferencesView : View() {

    private var locationBox by singleAssign<ComboBox<Location>>()
    private var daysBox by singleAssign<ComboBox<Int>>()
    private val model: PreferencesModel by inject()

    override val root = vbox(spacing = 10.0) {
        paddingAll = 5.0

        vbox {
            hbox(spacing = 3.0, alignment = Pos.CENTER_LEFT) {
                label("Location:")
                locationBox = combobox(values = model.locationOptions) {
                    selectionModel.select(model.location.get())
                    bindSelected(model.location)
                    cellFormat {
                        text = it.label
                    }
                }
            }
            hbox(spacing = 3.0, alignment = Pos.CENTER_LEFT) {
                label("Sync days:")
                daysBox = combobox(values = model.syncDaysOptions) {
                    selectionModel.select(model.syncDays.get() - 1) // HACK: just use the value as an index ;)
                    bindSelected(model.syncDays)
                }
            }
        }

        // TODO dirty model, disable button
        button("Save") {
            action {
                fire(SavePreferencesFXEvent)
            }
        }
        label("Restart the application after saving so changes will have an effect.")
        button("Export DB") {
            action {
                fire(ExportFXEvent)
            }
        }
    }
}
