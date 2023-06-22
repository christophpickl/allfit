package allfit.presentation.models

import allfit.presentation.logic.StaticIcon
import allfit.presentation.logic.StaticIconStorage
import allfit.presentation.search.HasCheckins
import allfit.presentation.search.HasRating
import allfit.presentation.search.HasTextSearchable
import allfit.service.Images
import allfit.service.ensureMaxLength
import java.time.ZonedDateTime
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

interface Partner : PartnerCustomAttributesWrite, HasRating, HasCheckins {
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

    override val checkins: Int
    fun checkinsProperty(): ObjectProperty<Int>

    val image: Image
    fun imageProperty(): ObjectProperty<Image>

    override var isFavorited: Boolean
    fun isFavoritedProperty(): ObjectProperty<Boolean>

    override var isWishlisted: Boolean
    fun isWishlistedProperty(): ObjectProperty<Boolean>

    var isHidden: Boolean
    fun isHiddenProperty(): ObjectProperty<Boolean>

    var hiddenImage: Image
    fun hiddenImageProperty(): ObjectProperty<Image>
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
    hiddenImage: Image,
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

    override var hiddenImage: Image by property(hiddenImage)
    override fun hiddenImageProperty() = getProperty(SimplePartner::hiddenImage)

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
        "SimplePartner[id=$id, name=$name, checkins=$checkins, rating=$rating, isFavorited=$isFavorited, " +
                "isWishlisted=$isWishlisted, isHidden=$isHidden, url=$url, categories=$categories, " +
                "facilities=$facilities, note=${note.ensureMaxLength(10)}, " +
                "description=${description.ensureMaxLength(10)}, image]"

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
    val pastCheckins: List<Checkin>,
    val upcomingWorkouts: List<SimpleWorkout>,
) : Partner by simplePartner, HasCheckins, HasTextSearchable, HasRating {

    override val searchableTerms = listOf(name)

    fun availability(usage: Usage): Int =
        usage.availabilityFor(pastCheckins, upcomingWorkouts)

    val lastCheckin: ZonedDateTime? =
        pastCheckins.maxByOrNull { it.date }?.date

    companion object {
        val prototype = FullPartner(
            simplePartner = SimplePartner(
                id = 0,
                name = "Partner",
                url = "",
                categories = emptyList(),
                note = "Note",
                description = "Description",
                facilities = "Facilities",
                checkins = 0,
                rating = 0,
                isFavorited = false,
                isWishlisted = false,
                isHidden = false,
                hiddenImage = NOT_HIDDEN_IMAGE,
                image = Images.prototype,
            ),
            pastCheckins = emptyList(),
            upcomingWorkouts = emptyList()
        )
    }
}

val HIDDEN_IMAGE = StaticIconStorage.get(StaticIcon.Hidden)
val NOT_HIDDEN_IMAGE = StaticIconStorage.get(StaticIcon.Empty)