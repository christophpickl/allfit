package allfit.sync

import allfit.api.InMemoryOnefitClient
import allfit.api.models.CategoriesJson
import allfit.api.models.CategoryJson
import allfit.api.models.categoryJson
import allfit.api.models.partnerCategoryJson
import allfit.api.models.partnerJson
import allfit.api.models.partnerSubCategoryJson
import allfit.api.models.partnersJson
import allfit.persistence.CategoryEntity
import allfit.persistence.InMemoryCategoriesRepo
import io.kotest.core.spec.style.StringSpec
import io.kotest.core.test.TestCase
import io.kotest.matchers.collections.shouldBeSingleton
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next

class CategoriesSyncerTest : StringSpec() {

    private val category = Arb.categoryJson().next()
    private val partnerCategory = Arb.partnerCategoryJson().next()
    private val partnerSubCategory = Arb.partnerSubCategoryJson().next()
    private val partners = Arb.partnersJson().next()
    private val partner = Arb.partnerJson().next()
    private val emptyPartnersJson = partners.copy(data = emptyList())
    private lateinit var syncer: CategoriesSyncer
    private lateinit var client: InMemoryOnefitClient
    private lateinit var categoriesRepo: InMemoryCategoriesRepo

    override suspend fun beforeEach(testCase: TestCase) {
        client = InMemoryOnefitClient()
        categoriesRepo = InMemoryCategoriesRepo()
        syncer = CategoriesSyncer(client, categoriesRepo)
    }

    init {
        "Given category JSON When sync Then insert it" {
            mockClientReturnsCategories(category)

            syncer.sync(emptyPartnersJson)

            categoriesRepo.selectAll().shouldBeSingleton().first() shouldBe CategoryEntity(
                id = category.id,
                name = category.name,
                isDeleted = false,
                slug = category.slugs.en,
            )
        }
        "When sync with partner Then insert categories from him" {
            syncer.sync(
                partners.copy(
                    data = listOf(
                        partner.copy(
                            category = partnerCategory,
                            categories = listOf(partnerSubCategory)
                        )
                    )
                )
            )

            categoriesRepo.selectAll().map { it.id } shouldContainExactlyInAnyOrder listOf(
                partnerCategory.id, partnerSubCategory.id
            )
        }
        "Given category JSON When sync with partner Then JSON gets precedence" {
            mockClientReturnsCategories(category.copy(name = "fromCategory"))

            syncer.sync(
                partners.copy(
                    data = listOf(
                        partner.copy(
                            category = partnerCategory.copy(id = category.id, name = "fromPartner"),
                            categories = emptyList(),
                        )
                    )
                )
            )

            categoriesRepo.selectAll().shouldBeSingleton().first().also {
                it.id shouldBe category.id
                it.name shouldBe "fromCategory"
                it.slug shouldBe category.slugs.en // partner would have possibly none!
            }
        }
    }

    private fun mockClientReturnsCategories(vararg categories: CategoryJson) {
        client.categoriesJson = CategoriesJson(categories.toList())
    }
}
