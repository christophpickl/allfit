package allfit.sync

import allfit.api.JsonLogFileManager
import allfit.api.NoOpJsonLogFileManager
import allfit.api.OnefitClient
import allfit.api.models.CategoriesJsonRoot
import allfit.api.models.CategoryJson
import allfit.api.models.PartnerCategoryJson
import allfit.api.models.PartnersJsonRoot
import allfit.api.models.partnerCategoryJson
import allfit.api.models.partnerJson
import allfit.persistence.DbListener
import allfit.persistence.domain.ExposedCategoriesRepo
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.core.test.TestCase
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.mockk.coEvery
import io.mockk.mockk

class CompositeSyncerTest : StringSpec() {

    private val syncException = Exception("sync failed")
    private lateinit var client: OnefitClient
    private lateinit var categoriesSyncer: CategoriesSyncer
    private lateinit var partnersSyncer: PartnersSyncer
    private lateinit var locationsSyncer: LocationsSyncer
    private lateinit var workoutsSyncer: WorkoutsSyncer
    private lateinit var reservationsSyncer: ReservationsSyncer
    private lateinit var checkinsSyncer: CheckinsSyncer
    private lateinit var syncer: CompositeSyncer
    private val jsonLogFileManager: JsonLogFileManager = NoOpJsonLogFileManager

    override suspend fun beforeEach(testCase: TestCase) {
        client = mockk()
        categoriesSyncer = CategoriesSyncerImpl(client, ExposedCategoriesRepo)
        partnersSyncer = mockk()
        locationsSyncer = mockk()
        workoutsSyncer = mockk()
        reservationsSyncer = mockk()
        checkinsSyncer = mockk()
        syncer = CompositeSyncer(
            client,
            categoriesSyncer,
            partnersSyncer,
            locationsSyncer,
            workoutsSyncer,
            reservationsSyncer,
            checkinsSyncer,
            SyncListenerManagerImpl(),
            jsonLogFileManager,
        )
    }

    init {
        extension(DbListener())

        "When categories inserted and partners fail Then categories insert is rolled back" {
            val partnerCategoryJson = Arb.partnerCategoryJson().next()
            val partnerJson = Arb.partnerJson().next().copy(
                category = partnerCategoryJson,
                categories = emptyList(),
            )
            val categoryJson = partnerCategoryJson.toCategoryJson()
            coEvery { client.getPartners(any()) } returns PartnersJsonRoot(listOf(partnerJson))
            coEvery { client.getCategories() } returns CategoriesJsonRoot(listOf(categoryJson))
            coEvery { partnersSyncer.sync(any()) } throws syncException

            shouldThrow<Exception> {
                syncer.syncAll()
            }.message shouldBe syncException.message

            ExposedCategoriesRepo.selectAll().shouldBeEmpty()
        }
    }
}

private fun PartnerCategoryJson.toCategoryJson() = CategoryJson(
    id = id,
    name = name,
    slugs = slugs,
)
