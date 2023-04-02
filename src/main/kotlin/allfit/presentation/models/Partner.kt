package allfit.presentation.models

import javafx.beans.property.ObjectProperty
import javafx.scene.image.Image
import tornadofx.getProperty
import tornadofx.property

interface Partner {
    val id: Int
    fun idProperty(): ObjectProperty<Int>

    val name: String
    fun nameProperty(): ObjectProperty<String>

    val url: String
    fun urlProperty(): ObjectProperty<String>

    val groups: List<String>
    fun groupsProperty(): ObjectProperty<List<String>>

    val description: String
    fun descriptionProperty(): ObjectProperty<String>

    val facilities: String
    fun facilitiesProperty(): ObjectProperty<String>

    var rating: Rating
    fun ratingProperty(): ObjectProperty<Rating>

    val checkins: Int
    fun checkinsProperty(): ObjectProperty<Int>

    val image: Image
    fun imageProperty(): ObjectProperty<Image>

    var isFavorited: Boolean
    fun isFavoritedProperty(): ObjectProperty<Boolean>

    var isWishlisted: Boolean
    fun isWishlistedProperty(): ObjectProperty<Boolean>

    var isHidden: Boolean
    fun isHiddenProperty(): ObjectProperty<Boolean>

    var note: String
    fun noteProperty(): ObjectProperty<String>
}

class SimplePartner(
    id: Int,
    name: String,
    url: String,
    groups: List<String>,
    note: String,
    description: String,
    facilities: String,
    checkins: Int,
    rating: Rating,
    isFavorited: Boolean,
    isWishlisted: Boolean,
    isHidden: Boolean,
    image: Image,
) : Partner {
    override var id: Int by property(id)
    override fun idProperty() = getProperty(SimplePartner::id)
    override var name: String by property(name)
    override fun nameProperty() = getProperty(SimplePartner::name)
    override var groups: List<String> by property(groups)
    override fun groupsProperty() = getProperty(SimplePartner::groups)
    override var checkins: Int by property(checkins)
    override fun checkinsProperty() = getProperty(SimplePartner::checkins)
    override var rating: Rating by property(rating)
    override fun ratingProperty() = getProperty(SimplePartner::rating)
    override var isFavorited: Boolean by property(isFavorited)
    override fun isFavoritedProperty() = getProperty(SimplePartner::isFavorited)
    override var isWishlisted: Boolean by property(isWishlisted)
    override fun isWishlistedProperty() = getProperty(SimplePartner::isWishlisted)
    override var isHidden: Boolean by property(isHidden)
    override fun isHiddenProperty() = getProperty(SimplePartner::isHidden)
    override var image: Image by property(image)
    override fun imageProperty() = getProperty(SimpleWorkout::image)
    override var note: String by property(note)
    override fun noteProperty() = getProperty(SimplePartner::note)
    override var description: String by property(description)
    override fun descriptionProperty() = getProperty(SimplePartner::description)
    override var facilities: String by property(facilities)
    override fun facilitiesProperty() = getProperty(SimplePartner::facilities)
    override var url: String by property(url)
    override fun urlProperty() = getProperty(SimplePartner::url)

    init {
        require(id >= 0) { "Invalid ID: $id" }
        require(rating in 0..5) { "Invalid rating: $rating" }
    }
}

data class FullPartner(
    val simplePartner: SimplePartner,
    val pastWorkouts: List<SimpleWorkout>,
    val currentWorkouts: List<SimpleWorkout>,
) : Partner by simplePartner
