package allfit.presentation.models

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import javafx.scene.image.Image
import tornadofx.SortedFilteredList
import tornadofx.ViewModel
import tornadofx.toObservable

class MainViewModel : ViewModel() {
    val allWorkouts = FXCollections.observableArrayList<FullWorkout>()!!
    val sortedFilteredWorkouts = SortedFilteredList<FullWorkout>(allWorkouts)
    val selectedPartner = CurrentPartnerViewModel()
    val selectedWorkout = SimpleObjectProperty<FullWorkout>()
    val allGroups = FXCollections.observableArrayList<String>()!!
}

class CurrentPartnerViewModel : ViewModel() {
    val id = SimpleIntegerProperty()
    val name = SimpleStringProperty()
    val rating = SimpleIntegerProperty()
    val image = SimpleObjectProperty<Image>()
    val note = SimpleStringProperty()
    val url = SimpleStringProperty()
    val facilities = SimpleStringProperty()
    val description = SimpleStringProperty()
    val isFavorited = SimpleBooleanProperty()
    val isWishlisted = SimpleBooleanProperty()
    val pastWorkouts = mutableListOf<SimpleWorkout>().toObservable()
    val currentWorkouts = mutableListOf<SimpleWorkout>().toObservable()

    fun initPartner(partner: FullPartner) {
        id.set(partner.id)
        name.set(partner.name)
        note.set(partner.note)
        rating.set(partner.rating)
        image.set(partner.image)
        url.set(partner.url)
        facilities.set(partner.facilities)
        description.set(partner.description)
        isFavorited.set(partner.isFavorited)
        isWishlisted.set(partner.isWishlisted)
        pastWorkouts.setAll(partner.pastWorkouts)
        currentWorkouts.setAll(partner.currentWorkouts)
    }
}
