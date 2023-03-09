package allfit.service

import allfit.domain.Category
import allfit.persistence.CategoriesRepo
import allfit.persistence.CategoryDbo

class DataStorage(
    private val categoriesRepo: CategoriesRepo
) {
    private val lazyCategories: List<Category> by lazy {
        categoriesRepo.select().filter { !it.isDeleted }.map { it.toCategory() }
    }

    fun getCategories(): List<Category> = lazyCategories
}

private fun CategoryDbo.toCategory() = Category(
    id = id,
    name = name,
)
