package allfit.persistence

import allfit.domain.Category
import allfit.domain.Partner

object InMemoryMockDataSetup {
    private val category1 = Category(id = 1, name = "Foo", isDeleted = false)
    private val category2 = Category(id = 2, name = "Bar", isDeleted = false)
    private val categories = listOf(category1, category2)
    private val partners = listOf(
        Partner(id = 1, name = "Partner A", isDeleted = false, categories = listOf(category1)),
        Partner(id = 2, name = "Partner B", isDeleted = false, categories = listOf(category2)),
    )

    fun InMemoryCategoriesRepo.insertMockData() = apply {
        insert(categories)
    }

    fun InMemoryPartnersRepo.insertMockData() = apply {
        insert(partners)
    }
}
