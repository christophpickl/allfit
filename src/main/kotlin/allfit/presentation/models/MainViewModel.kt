package allfit.presentation.models

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import tornadofx.ViewModel

class MainViewModel : ViewModel() {
    val allWorkouts = FXCollections.observableArrayList<FullWorkout>()!!

    //    val sortedFilteredWorkouts = SortedFilteredList<WorkoutViewModel>()
    val selectedPartner = CurrentPartnerViewModel()
    val selectedWorkout = SimpleObjectProperty<FullWorkout>()
}

class CurrentPartnerViewModel : ViewModel() {
    val id = SimpleIntegerProperty()
    val name = SimpleStringProperty()
    val rating = SimpleIntegerProperty()
    val isFavourited = SimpleBooleanProperty()
    val isWishlisted = SimpleBooleanProperty()

    fun initPartner(partner: FullPartner) {
        id.set(partner.id)
        name.set(partner.name)
        rating.set(partner.rating)
        isFavourited.set(partner.isFavourited)
        isWishlisted.set(partner.isWishlisted)
    }
}
