package allfit.presentation.models

import allfit.service.Images
import javafx.beans.property.ObjectProperty
import javafx.scene.image.Image
import tornadofx.getProperty
import tornadofx.property

interface PartnerCustomAttributesRead {
    val rating: Rating
    val note: String
    val isFavorited: Boolean
    val isWishlisted: Boolean
}

interface PartnerCustomAttributesWrite : PartnerCustomAttributesRead {
    override var rating: Rating
    override var note: String
    override var isFavorited: Boolean
    override var isWishlisted: Boolean
}

interface Partner : PartnerCustomAttributesWrite {
    val id: Int
    fun idProperty(): ObjectProperty<Int>

    val name: String
    fun nameProperty(): ObjectProperty<String>

    val description: String
    fun descriptionProperty(): ObjectProperty<String>

    override var note: String
    fun noteProperty(): ObjectProperty<String>

    val url: String
    fun urlProperty(): ObjectProperty<String>

    val categories: List<String>
    fun categoriesProperty(): ObjectProperty<List<String>>

    val facilities: String
    fun facilitiesProperty(): ObjectProperty<String>

    override var rating: Rating
    fun ratingProperty(): ObjectProperty<Rating>

    val checkins: Int
    fun checkinsProperty(): ObjectProperty<Int>

    val image: Image
    fun imageProperty(): ObjectProperty<Image>

    override var isFavorited: Boolean
    fun isFavoritedProperty(): ObjectProperty<Boolean>

    override var isWishlisted: Boolean
    fun isWishlistedProperty(): ObjectProperty<Boolean>

    var isHidden: Boolean
    fun isHiddenProperty(): ObjectProperty<Boolean>
}

class SimplePartner(
    id: Int,
    name: String,
    url: String,
    categories: List<String>,
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
    override var categories: List<String> by property(categories)
    override fun categoriesProperty() = getProperty(SimplePartner::categories)
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

    override fun toString() =
        "SimplePartner[id=$id, name=$name, url=$url, categories=$categories, note=$note, description=$description, facilities=$facilities, checkins=$checkins, rating=$rating, isFavorited=$isFavorited, isWishlisted=$isWishlisted, isHidden=$isHidden, image]"

    override fun equals(other: Any?): Boolean {
        if (other == null) return false
        if (this === other) return true
        if (other !is SimplePartner) return false
        return id == other.id &&
                name == other.name &&
                url == other.url &&
                categories == other.categories &&
                note == other.note &&
                description == other.description &&
                facilities == other.facilities &&
                checkins == other.checkins &&
                rating == other.rating &&
                isFavorited == other.isFavorited &&
                isWishlisted == other.isWishlisted &&
                isHidden == other.isHidden &&
                image == other.image
    }

    override fun hashCode() = id.hashCode()
}

data class FullPartner(
    val simplePartner: SimplePartner,
    val visitedWorkouts: List<SimpleWorkout>,
    val upcomingWorkouts: List<SimpleWorkout>,
) : Partner by simplePartner {
    companion object {
        val prototype = FullPartner(
            simplePartner = SimplePartner(
                id = 0,
                name = "Partner",
                url = "",
                categories = listOf(),
                note = "Note",
                description = "Description",
                facilities = "Facilities",
                checkins = 0,
                rating = 0,
                isFavorited = false,
                isWishlisted = false,
                isHidden = false,
                image = Images.prototype,
            ),
            visitedWorkouts = listOf(),
            upcomingWorkouts = listOf()
        )
    }
}
