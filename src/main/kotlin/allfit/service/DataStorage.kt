package allfit.service

import allfit.domain.Category
import allfit.persistence.CategoriesRepo

class DataStorage(
    private val categoriesRepo: CategoriesRepo
) {
    private val lazyCategories: List<Category> by lazy {
        categoriesRepo.select().filter { !it.isDeleted }
    }

    fun getCategories(): List<Category> = lazyCategories
}
