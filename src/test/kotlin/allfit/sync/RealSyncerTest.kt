package allfit.sync

import allfit.api.InMemoryOnefitClient
import allfit.api.models.CategoriesJson
import allfit.api.models.PartnersJson
import allfit.api.models.WorkoutJson
import allfit.api.models.categoryJson
import allfit.api.models.partnerCategoryJson
import allfit.api.models.partnerJson
import allfit.api.models.workoutJson
import allfit.api.models.workoutsJson
import allfit.persistence.InMemoryCategoriesRepo
import allfit.persistence.InMemoryPartnersRepo
import allfit.persistence.InMemoryWorkoutsRepo
import allfit.persistence.workoutEntity
import allfit.service.toUtcLocalDateTime
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.core.test.TestCase
import io.kotest.matchers.collections.shouldBeSingleton
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next

class RealSyncerTest : DescribeSpec() {

    private lateinit var client: InMemoryOnefitClient
    private lateinit var categoriesRepo: InMemoryCategoriesRepo
    private lateinit var partnersRepo: InMemoryPartnersRepo
    private lateinit var workoutsRepo: InMemoryWorkoutsRepo

    private val category = Arb.categoryJson().next()
    private val partner = Arb.partnerJson().next()
    private val partnerCategory1 = Arb.partnerCategoryJson().next().copy(id = 101)
    private val partnerCategory2 = Arb.partnerCategoryJson().next().copy(id = 102)

    override suspend fun beforeEach(testCase: TestCase) {
        client = InMemoryOnefitClient()
        categoriesRepo = InMemoryCategoriesRepo()
        partnersRepo = InMemoryPartnersRepo()
        workoutsRepo = InMemoryWorkoutsRepo()
    }

    init {
        describe("Categories") {
            it("Insert category") {
                client.categoriesJson = CategoriesJson(listOf(category))

                syncAll()

                categoriesRepo.selectAll().shouldBeSingleton().first().id shouldBe category.id
            }
        }
        describe("Partners") {
            it("Insert partner") {
                client.partnersJson = PartnersJson(listOf(partner))

                syncAll()

                partnersRepo.selectAll().shouldBeSingleton().first().id shouldBe partner.id
            }
            it("Insert category") {
                val partnerWithCategories = partner.copy(
                    category = partnerCategory1,
                    categories = listOf(partnerCategory2)
                )
                client.partnersJson = PartnersJson(listOf(partnerWithCategories))

                syncAll()

                categoriesRepo.selectAll().map { it.id } shouldContainExactlyInAnyOrder listOf(
                    partnerCategory1,
                    partnerCategory2
                ).map { it.id }
            }
        }

        describe("Workouts") {
            it("Given workout json Then insert workout") {
                val workoutJson = Arb.workoutJson().next()
                client.mockWorkoutsResponse(workoutJson)

                syncAll()

                workoutsRepo.selectAllStartingFrom(workoutJson.from.toUtcLocalDateTime()).shouldBeSingleton()
                    .first().id shouldBe workoutJson.id
            }
            it("Given workout already in DB Then do nothing") {
                val workoutEntity = Arb.workoutEntity().next()
                workoutsRepo.insertAll(listOf(workoutEntity))
                val workoutJson = Arb.workoutJson().next().copy(id = workoutEntity.id)
                client.mockWorkoutsResponse(workoutJson)

                syncAll()

                workoutsRepo.selectAllStartingFrom(workoutJson.from.toUtcLocalDateTime()).shouldBeSingleton()
                    .first() shouldBe workoutEntity
            }
        }
    }

    private suspend fun syncAll() = RealSyncer(client, categoriesRepo, partnersRepo, workoutsRepo).syncAll()
}

private fun InMemoryOnefitClient.mockWorkoutsResponse(vararg workouts: WorkoutJson) {
    workoutsJson = Arb.workoutsJson().next().copy(data = workouts.toList())
}
