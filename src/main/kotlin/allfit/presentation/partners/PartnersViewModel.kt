package allfit.presentation.partners

import allfit.presentation.models.FullPartner
import allfit.presentation.models.FullWorkout
import allfit.presentation.workouts.CurrentPartnerViewModel
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.FXCollections
import tornadofx.SortedFilteredList
import tornadofx.ViewModel

class PartnersViewModel : ViewModel() {
    private val rawPartners = FXCollections.observableArrayList<FullPartner>()!!
    val sortedFilteredPartners = SortedFilteredList<FullPartner>(rawPartners)
    val selectedPartner = CurrentPartnerViewModel()
    val selectedWorkout = SimpleObjectProperty<FullWorkout>()
}
