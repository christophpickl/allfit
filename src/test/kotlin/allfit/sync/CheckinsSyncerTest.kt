package allfit.sync

import allfit.api.InMemoryOnefitClient
import allfit.api.models.CheckinJson
import allfit.api.models.CheckinsJsonRoot
import allfit.api.models.MetaJson
import allfit.api.models.checkinJson
import allfit.persistence.domain.CheckinEntity
import allfit.persistence.domain.ExposedCategoriesRepo
import allfit.persistence.domain.InMemoryCategoriesRepo
import allfit.persistence.domain.InMemoryCheckinsRepository
import allfit.persistence.domain.InMemoryPartnersRepo
import allfit.persistence.domain.InMemoryWorkoutsRepo
import allfit.persistence.testInfra.categoryEntity
import allfit.persistence.testInfra.checkinEntity
import allfit.persistence.testInfra.partnerEntity
import allfit.persistence.testInfra.singleShould
import allfit.persistence.testInfra.singletonShouldBe
import allfit.persistence.testInfra.workoutEntity
import allfit.service.InMemoryImageStorage
import allfit.service.PartnerAndImageUrl
import allfit.service.WorkoutAndImageUrl
import allfit.service.toUtcLocalDateTime
import io.kotest.core.spec.style.StringSpec
import io.kotest.core.test.TestCase
import io.kotest.matchers.collections.shouldBeSingleton
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import java.util.UUID

class CheckinsSyncerTest : StringSpec() {

    private val checkinJson = Arb.checkinJson().next()
    private lateinit var syncer: CheckinsSyncer
    private lateinit var client: InMemoryOnefitClient
    private lateinit var checkinsRepo: InMemoryCheckinsRepository
    private lateinit var workoutsRepo: InMemoryWorkoutsRepo
    private lateinit var partnersRepo: InMemoryPartnersRepo
    private lateinit var categoriesRepo: InMemoryCategoriesRepo
    private lateinit var imageStorage: InMemoryImageStorage

    override suspend fun beforeEach(testCase: TestCase) {
        client = InMemoryOnefitClient()
        checkinsRepo = InMemoryCheckinsRepository()
        workoutsRepo = InMemoryWorkoutsRepo()
        partnersRepo = InMemoryPartnersRepo()
        categoriesRepo = InMemoryCategoriesRepo()
        imageStorage = InMemoryImageStorage()
        syncer = CheckinsSyncerImpl(client, checkinsRepo, workoutsRepo, partnersRepo, categoriesRepo, imageStorage)
    }

    init {
        "Given nothing but checkin When sync Then insert workout" {
            client.mockCheckins(checkinJson)

            syncer.sync()

            workoutsRepo.singleShould().id shouldBe checkinJson.workout.id
        }
        "Given nothing but checkin When sync Then insert workout and partner image" {
            client.mockCheckins(checkinJson)

            syncer.sync()

            imageStorage.savedPartnerImages.shouldBeSingleton().first() shouldBe PartnerAndImageUrl(
                checkinJson.workout.partner.id,
                checkinJson.workout.partner.id.toString()
            )
            imageStorage.savedWorkoutImages.shouldBeSingleton()
                .first() shouldBe WorkoutAndImageUrl(checkinJson.workout.id, checkinJson.workout.id.toString())
        }
        "Given nothing but checkin When sync Then insert partner" {
            client.mockCheckins(checkinJson)

            syncer.sync()

            partnersRepo.singleShould().id shouldBe checkinJson.workout.partner.id
        }
        "Given nothing but checkin When sync Then insert category" {
            client.mockCheckins(checkinJson)

            syncer.sync()

            categoriesRepo.singleShould().id shouldBe checkinJson.workout.partner.category.id
        }
        "Given nothing but checkin When sync Then insert checkin" {
            client.mockCheckins(checkinJson)

            syncer.sync()

            checkinsRepo.checkins.shouldBeSingleton().first() shouldBe CheckinEntity(
                id = UUID.fromString(checkinJson.uuid),
                workoutId = checkinJson.workout.id,
                createdAt = checkinJson.created_at.toUtcLocalDateTime(),
            )
        }
        "Given requirements exist for new checkin When sync Then only insert checkin" {
            val category = Arb.categoryEntity().next()
            ExposedCategoriesRepo.insertAll(listOf(category))
            val partner = Arb.partnerEntity().next().copy(
                id = checkinJson.workout.partner.id,
                primaryCategoryId = category.id,
                secondaryCategoryIds = emptyList()
            )
            partnersRepo.insertAll(listOf(partner))
            val workout = Arb.workoutEntity().next().copy(id = checkinJson.workout.id, partnerId = partner.id)
            workoutsRepo.insertAll(listOf(workout))
            client.mockCheckins(checkinJson)

            syncer.sync()

            partnersRepo singletonShouldBe partner
            workoutsRepo singletonShouldBe workout
            checkinsRepo.singleShould().id.toString() shouldBe checkinJson.uuid
        }
        "Given workout exists When sync Then skip it" {
            val yetExisting = Arb.checkinEntity().next().copy(id = UUID.fromString(checkinJson.uuid))
            checkinsRepo.insertAll(listOf(yetExisting))
            client.mockCheckins(checkinJson)

            syncer.sync()

            checkinsRepo singletonShouldBe yetExisting
        }
    }

    private fun InMemoryOnefitClient.mockCheckins(vararg checkins: CheckinJson) {
        client.checkinsJson = CheckinsJsonRoot(data = checkins.toList(), meta = MetaJson.empty)
    }
}