package allfit.domain

data class Partner(
    override val id: Int,
    override val isDeleted: Boolean,
    val name: String,
    val categories: List<Category>,
    // description
    // image
    // location / address

    // CUSTOM fields:
    // isHidden
    // notes
    // rating: Int 1-5?
    // visitCount: Int 0-*
    // starred: Boolean
    // favorite: Boolean
) : BaseDomain

fun Map<Int, Partner>.findOrThrow(id: Int) =
    this[id] ?: throw PartnerNotFoundException("Could not find partner by ID: $id!")

class PartnerNotFoundException(message: String) : Exception(message)
