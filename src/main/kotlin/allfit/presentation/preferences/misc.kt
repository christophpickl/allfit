package allfit.presentation.preferences

import allfit.domain.Location
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.FXCollections
import tornadofx.FXEvent
import tornadofx.ViewModel

class PreferencesModel : ViewModel() {

    val locationOptions = FXCollections.observableArrayList<Location>()!!
    val location = SimpleObjectProperty(Location.DEFAULT)

    val syncDaysOptions = FXCollections.observableArrayList<Int>()!!
    val syncDays = SimpleObjectProperty(0)
}

object SavePreferencesFXEvent : FXEvent()
object ExportFXEvent : FXEvent()
