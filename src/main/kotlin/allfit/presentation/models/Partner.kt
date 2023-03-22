package allfit.presentation.models

import tornadofx.getProperty
import tornadofx.property

typealias Rating = Int

interface Partner {
    val id: Int
    val name: String
    val visits: Int
    var rating: Rating
    var isFavourited: Boolean
    var isWishlisted: Boolean
}

class SimplePartner(
    id: Int,
    name: String,
    visits: Int,
    rating: Rating,
    isFavourited: Boolean,
    isWishlisted: Boolean,
) : Partner {
    override var id: Int by property(id)
    fun idProperty() = getProperty(SimplePartner::id)
    override var name: String by property(name)
    fun nameProperty() = getProperty(SimplePartner::name)
    override var visits: Int by property(visits)
    fun visitsProperty() = getProperty(SimplePartner::visits)
    override var rating: Rating by property(rating)
    fun ratingProperty() = getProperty(SimplePartner::rating)
    override var isFavourited: Boolean by property(isFavourited)
    fun isFavouritedProperty() = getProperty(SimplePartner::isFavourited)
    override var isWishlisted: Boolean by property(isWishlisted)
    fun isWishlistedProperty() = getProperty(SimplePartner::isWishlisted)

    init {
        require(rating in 0..5) { "Invalid rating: $rating" }
    }
}

data class FullPartner(
    val simplePartner: SimplePartner,
    val workouts: List<SimpleWorkout>
) : Partner by simplePartner
