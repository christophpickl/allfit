package allfit.presentation.models

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.beans.value.ObservableValue
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

    companion object {
        val DEFAULT_WORKOUT_PREDICATE: (FullWorkout) -> Boolean = {
            !it.partner.isHidden
        }
    }
}

class CurrentPartnerViewModel : ViewModel() {
    val id = SimpleIntegerProperty()
    val name = SimpleStringProperty()
    val rating = SimpleIntegerProperty()
    val image = SimpleObjectProperty<Image>()
    val note = SimpleStringProperty()
    val url = SimpleStringProperty()
    val facilities = SimpleStringProperty()
    val facilitiesRendered: ObservableValue<String> = facilities.map { facilities ->
        facilities.split(",").joinToString(", ").let {
            it.ifEmpty { "None" }
        }
    }
    val categories = SimpleObjectProperty<List<String>>()
    val categoriesRendered: ObservableValue<String> = categories.map { categories ->
        if (categories.isEmpty()) "None" else categories.joinToString()
    }
    val description = SimpleStringProperty()
    val isFavorited = SimpleBooleanProperty()
    val isWishlisted = SimpleBooleanProperty()
    val pastCheckins = mutableListOf<Checkin>().toObservable()
    val upcomingWorkouts = mutableListOf<SimpleWorkout>().toObservable()

    fun initPartner(partner: FullPartner) {
        id.set(partner.id)
        name.set(partner.name)
        note.set(partner.note)
        rating.set(partner.rating)
        image.set(partner.image)
        url.set(partner.url)
        facilities.set(partner.facilities)
        categories.set(partner.categories)
        description.set(partner.description)
        isFavorited.set(partner.isFavorited)
        isWishlisted.set(partner.isWishlisted)
        pastCheckins.setAll(partner.pastCheckins)
        upcomingWorkouts.setAll(partner.upcomingWorkouts)
    }
}
