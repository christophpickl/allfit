package allfit.sync.domain

import allfit.api.InMemoryOnefitClient
import allfit.api.models.CheckinJson
import allfit.api.models.CheckinsJsonRoot
import allfit.api.models.MetaJson
import allfit.api.models.checkinJson
import allfit.api.models.checkinJsonDropin
import allfit.api.models.checkinJsonWorkout
import allfit.persistence.domain.CheckinEntity
import allfit.persistence.domain.CheckinType
import allfit.persistence.domain.InMemoryCategoriesRepo
import allfit.persistence.domain.InMemoryCheckinsRepository
import allfit.persistence.domain.InMemoryPartnersRepo
import allfit.persistence.domain.InMemorySinglesRepo
import allfit.persistence.domain.InMemoryWorkoutsRepo
import allfit.persistence.testInfra.categoryEntity
import allfit.persistence.testInfra.checkinEntityWorkout
import allfit.persistence.testInfra.partnerEntity
import allfit.persistence.testInfra.singleShould
import allfit.persistence.testInfra.singletonShouldBe
import allfit.persistence.testInfra.workoutEntity
import allfit.service.InMemoryImageStorage
import allfit.service.PartnerAndImageUrl
import allfit.service.toUtcLocalDateTime
import allfit.sync.core.InMemorySyncListenerManager
import io.kotest.core.spec.style.StringSpec
import io.kotest.core.test.TestCase
import io.kotest.matchers.collections.shouldBeSingleton
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import java.util.UUID

class CheckinsSyncerTest : StringSpec() {

    private val checkinJson = Arb.checkinJson().next()
    private val checkinJsonWorkout = Arb.checkinJsonWorkout().next()
    private val checkinJsonDropin = Arb.checkinJsonDropin().next()
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
        syncer = CheckinsSyncerImpl(
            client,
            checkinsRepo,
            workoutsRepo,
            partnersRepo,
            categoriesRepo,
            imageStorage,
            InMemorySinglesRepo(),
            InMemorySyncListenerManager(),
        )
    }

    init {
        "Given workout-checkin When sync Then insert workout" {
            client.mockCheckins(checkinJsonWorkout)

            syncer.sync()

            workoutsRepo.singleShould().id shouldBe checkinJsonWorkout.workout!!.id
        }
        "Given workout-checkin When sync Then insert images" {
            client.mockCheckins(checkinJsonWorkout)

            syncer.sync()

            imageStorage.savedPartnerImages.shouldBeSingleton().first() shouldBe PartnerAndImageUrl(
                checkinJsonWorkout.workout!!.partner.id,
                checkinJsonWorkout.workout!!.partner.id.toString()
            )
        }
        "Given workout-checkin When sync Then insert partner" {
            client.mockCheckins(checkinJsonWorkout)

            syncer.sync()

            partnersRepo.singleShould().id shouldBe checkinJsonWorkout.workout!!.partner.id
        }
        "Given workout-checkin When sync Then insert category" {
            client.mockCheckins(checkinJsonWorkout)

            syncer.sync()

            categoriesRepo.singleShould().id shouldBe checkinJsonWorkout.workout!!.partner.category.id
        }
        "Given workout-checkin When sync Then insert checkin" {
            client.mockCheckins(checkinJsonWorkout)

            syncer.sync()

            checkinsRepo.checkins.shouldBeSingleton().first() shouldBe CheckinEntity(
                id = UUID.fromString(checkinJsonWorkout.uuid),
                createdAt = checkinJsonWorkout.created_at.toUtcLocalDateTime(),
                type = CheckinType.WORKOUT,
                partnerId = checkinJsonWorkout.typeSafePartner.id,
                workoutId = checkinJsonWorkout.workout!!.id,
            )
        }
        "Given workout-checkout exists When sync Then skip it" {
            val yetExisting = Arb.checkinEntityWorkout().next().copy(id = UUID.fromString(checkinJson.uuid))
            checkinsRepo.insertAll(listOf(yetExisting))
            client.mockCheckins(checkinJson)

            syncer.sync()

            checkinsRepo singletonShouldBe yetExisting
        }

        "Given dropin-checkin When sync Then insert partner" {
            client.mockCheckins(checkinJsonDropin)

            syncer.sync()

            partnersRepo.singleShould().id shouldBe checkinJsonDropin.partner!!.id
        }
        "Given dropin-checkin When sync Then insert category" {
            client.mockCheckins(checkinJsonDropin)

            syncer.sync()

            categoriesRepo.singleShould().id shouldBe checkinJsonDropin.partner!!.category.id
        }
        "Given dropin-checkin When sync Then insert checkin" {
            client.mockCheckins(checkinJsonDropin)

            syncer.sync()

            checkinsRepo.checkins.shouldBeSingleton().first() shouldBe CheckinEntity(
                id = UUID.fromString(checkinJsonDropin.uuid),
                createdAt = checkinJsonDropin.created_at.toUtcLocalDateTime(),
                type = CheckinType.DROP_IN,
                partnerId = checkinJsonDropin.typeSafePartner.id,
                workoutId = null,
            )
        }
        "Given requirements exist for new workout-checkin When sync Then only insert checkin" {
            val category = Arb.categoryEntity().next()
            categoriesRepo.insertAll(listOf(category))
            val partner = Arb.partnerEntity().next().copy(
                id = checkinJsonWorkout.typeSafePartner.id,
                primaryCategoryId = category.id,
                secondaryCategoryIds = emptyList()
            )
            partnersRepo.insertAll(listOf(partner))
            val workout = Arb.workoutEntity().next().copy(id = checkinJsonWorkout.workout!!.id, partnerId = partner.id)
            workoutsRepo.insertAll(listOf(workout))
            client.mockCheckins(checkinJsonWorkout)

            syncer.sync()

            partnersRepo singletonShouldBe partner
            workoutsRepo singletonShouldBe workout
            checkinsRepo.singleShould().id.toString() shouldBe checkinJsonWorkout.uuid
        }
    }

    private fun InMemoryOnefitClient.mockCheckins(vararg checkins: CheckinJson) {
        client.checkinsJson = CheckinsJsonRoot(data = checkins.toList(), meta = MetaJson.empty)
    }
}