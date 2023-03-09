package allfit.domain

data class Categories(
    val categories: List<Category>
)

data class Category(
    val id: Int,
    val shortCode: String
)
