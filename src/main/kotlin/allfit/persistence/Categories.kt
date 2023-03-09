package allfit.persistence

import allfit.domain.Categories

interface CategoriesRepo {
    fun load(): Categories
}

object InMemoryCategoriesRepo : CategoriesRepo {
    override fun load() = Categories(emptyList())
}
