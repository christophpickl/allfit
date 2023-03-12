package allfit.service

import allfit.domain.Category
import allfit.persistence.CategoriesRepo
import allfit.persistence.CategoryEntity

class DataStorage(
    private val categoriesRepo: CategoriesRepo
) {
    private val lazyCategories: List<Category> by lazy {
        categoriesRepo.selectAll().filter { !it.isDeleted }.map { it.toCategory() }
    }

    fun getCategories(): List<Category> = lazyCategories
}

private fun CategoryEntity.toCategory() = Category(
    id = id,
    name = name,
    isDeleted = isDeleted,
)
