package allfit.presentation

import allfit.presentation.models.Rating
import allfit.presentation.models.SimplePartner

private val starsMap = mapOf(
    0 to "",
    1 to "⭐️",
    2 to "⭐️⭐️",
    3 to "⭐️⭐️⭐️",
    4 to "⭐️⭐️⭐️⭐️",
    5 to "⭐️⭐️⭐️⭐️⭐️",
)

fun Rating.renderStars() = starsMap[this] ?: error("Invalid rating star entry: $this")

data class PartnerModifications(
    val partnerId: Int,
    val rating: Rating,
    val isFavourited: Boolean,
    val isWishlisted: Boolean,
) {
    fun update(storedPartner: SimplePartner) {
        storedPartner.rating = rating
        storedPartner.isFavourited = isFavourited
        storedPartner.isWishlisted = isWishlisted
    }
}
