package allfit.persistence

object InMemoryMockDataSetup {
    private val category1 = CategoryDbo(id = 1, name = "Foo", isDeleted = false)
    private val category2 = CategoryDbo(id = 2, name = "Bar", isDeleted = false)
    private val categories = listOf(category1, category2)
    private val partners = listOf(
        PartnerDbo(id = 1, name = "Partner A", isDeleted = false, categories = listOf(category1.id)),
        PartnerDbo(id = 2, name = "Partner B", isDeleted = false, categories = listOf(category2.id)),
    )

    fun InMemoryCategoriesRepo.insertMockData() = apply {
        insert(categories)
    }

    fun InMemoryPartnersRepo.insertMockData() = apply {
        insert(partners)
    }
}
