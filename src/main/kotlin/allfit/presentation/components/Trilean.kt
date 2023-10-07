package allfit.presentation.components

import allfit.presentation.models.Trilean
import javafx.event.EventTarget
import javafx.scene.control.ComboBox
import tornadofx.FX
import tornadofx.cellFormat
import tornadofx.combobox

typealias TrileanBox = ComboBox<Trilean>

fun EventTarget.trileanbox(selectionListener: () -> Unit) = combobox(values = Trilean.entries) {
    selectionModel.selectFirst()
    cellFormat(FX.defaultScope) {
        text = when (it) {
            Trilean.Yes -> "Enabled"
            Trilean.No -> "Disabled"
            Trilean.Unknown -> "Unknown"
        }
        selectedProperty().addListener { _ ->
            selectionListener()
        }
    }
}
