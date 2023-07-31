package allfit.presentation.models

import allfit.presentation.partners.PartnerDetailModel
import allfit.presentation.workouts.CurrentPartnerViewModel
import allfit.presentation.workouts.WorkoutDetailModel
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.FXCollections
import tornadofx.SortedFilteredList
import tornadofx.ViewModel

class PartnersViewModel : ViewModel(), PartnerDetailModel, WorkoutDetailModel {
    val allPartners = FXCollections.observableArrayList<FullPartner>()!!
    val sortedFilteredPartners = SortedFilteredList<FullPartner>(allPartners)
    override val selectedPartner = CurrentPartnerViewModel()
    override val selectedWorkout = SimpleObjectProperty<FullWorkout>()
}
