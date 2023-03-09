package allfit.sync

import allfit.api.models.CategoriesJson
import allfit.api.models.partnerCategoryJson
import allfit.persistence.categoryDbo
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldBeSingleton
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next

class SyncDifferTest : DescribeSpec() {

    private val categoryId = 42

    init {
        describe("Insert categories") {
            it("Insert single") {
                val report = SyncDiffer.diffCategories(localCategoriesWithId(), remoteCategoriesWithId(categoryId))

                report.toInsert.shouldBeSingleton().first().id shouldBe categoryId
            }
            it("Skip existing") {
                val report = SyncDiffer.diffCategories(
                    localCategoriesWithId(categoryId),
                    remoteCategoriesWithId(categoryId)
                )

                report.toInsert.shouldBeEmpty()
            }
        }
        describe("Delete categories") {
            it("Delete single") {
                val report = SyncDiffer.diffCategories(localCategoriesWithId(categoryId), remoteCategoriesWithId())

                report.toDelete.shouldBeSingleton().first().id shouldBe categoryId
            }
            it("Skip existing") {
                val report = SyncDiffer.diffCategories(
                    localCategoriesWithId(categoryId),
                    remoteCategoriesWithId(categoryId)
                )

                report.toDelete.shouldBeEmpty()
            }
        }
    }

    private fun localCategoriesWithId(vararg ids: Int) = ids.map { id ->
        Arb.categoryDbo().next().copy(id = id)
    }

    private fun remoteCategoriesWithId(vararg ids: Int) = CategoriesJson(ids.map { id ->
        Arb.partnerCategoryJson().next().copy(id = id)
    })
}
