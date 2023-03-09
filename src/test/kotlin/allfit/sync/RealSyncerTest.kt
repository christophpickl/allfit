package allfit.sync

import allfit.api.InMemoryOnefitClient
import allfit.api.models.CategoriesJson
import allfit.api.models.PartnersJson
import allfit.api.models.categoryJson
import allfit.api.models.partnerJson
import allfit.persistence.InMemoryCategoriesRepo
import allfit.persistence.InMemoryPartnersRepo
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.core.test.TestCase
import io.kotest.matchers.collections.shouldBeSingleton
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next

class RealSyncerTest : DescribeSpec() {

    private lateinit var client: InMemoryOnefitClient
    private lateinit var categoriesRepo: InMemoryCategoriesRepo
    private lateinit var partnersRepo: InMemoryPartnersRepo

    override suspend fun beforeEach(testCase: TestCase) {
        client = InMemoryOnefitClient()
        categoriesRepo = InMemoryCategoriesRepo()
        partnersRepo = InMemoryPartnersRepo()
    }

    private val categoryJson = Arb.categoryJson().next()
    private val partnerJson = Arb.partnerJson().next()

    init {
        describe("Categories") {
            it("Insert") {
                client.categoriesJson = CategoriesJson(listOf(categoryJson))

                syncAll()

                categoriesRepo.select().shouldBeSingleton().first().id shouldBe categoryJson.id
            }
        }
        describe("Partners") {
            it("Insert") {
                client.partnersJson = PartnersJson(listOf(partnerJson))

                syncAll()

                partnersRepo.select().shouldBeSingleton().first().id shouldBe partnerJson.id
            }
        }
    }

    private suspend fun syncAll() = RealSyncer(client, categoriesRepo, partnersRepo).syncAll()
}
