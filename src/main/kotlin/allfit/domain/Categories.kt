package allfit.domain

data class Category(
    override val id: Int,
    val name: String,
    override val isDeleted: Boolean,
) : BaseDomain
