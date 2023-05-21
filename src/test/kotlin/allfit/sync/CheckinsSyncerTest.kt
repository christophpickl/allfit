package allfit.sync

import allfit.api.InMemoryOnefitClient
import allfit.api.models.*
import allfit.persistence.domain.*
import allfit.persistence.testInfra.*
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
import java.util.*

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
        syncer = CheckinsSyncerImpl(client, checkinsRepo, workoutsRepo, partnersRepo, categoriesRepo, imageStorage)
    }

    init {
        "Given nothing but workout-checkin When sync Then insert workout" {
            client.mockCheckins(checkinJsonWorkout)

            syncer.sync()

            workoutsRepo.singleShould().id shouldBe checkinJsonWorkout.workout!!.id
        }
        "Given nothing but workout-checkin When sync Then insert workout and partner image" {
            client.mockCheckins(checkinJsonWorkout)

            syncer.sync()

            imageStorage.savedPartnerImages.shouldBeSingleton().first() shouldBe PartnerAndImageUrl(
                checkinJsonWorkout.workout!!.partner.id,
                checkinJsonWorkout.workout!!.partner.id.toString()
            )
            imageStorage.savedWorkoutImages.shouldBeSingleton()
                .first() shouldBe WorkoutAndImageUrl(
                checkinJsonWorkout.workout!!.id,
                checkinJsonWorkout.workout!!.id.toString()
            )
        }
        "Given nothing but workout-checkin When sync Then insert partner" {
            client.mockCheckins(checkinJsonWorkout)

            syncer.sync()

            partnersRepo.singleShould().id shouldBe checkinJsonWorkout.workout!!.partner.id
        }
        "Given nothing but dropin-checkin When sync Then insert partner" {
            client.mockCheckins(checkinJsonDropin)

            syncer.sync()

            partnersRepo.singleShould().id shouldBe checkinJsonDropin.partner!!.id
        }
        "Given nothing but workout-checkin When sync Then insert category" {
            client.mockCheckins(checkinJsonWorkout)

            syncer.sync()

            categoriesRepo.singleShould().id shouldBe checkinJsonWorkout.workout!!.partner.category.id
        }
        "Given nothing but dropin-checkin When sync Then insert category" {
            client.mockCheckins(checkinJsonDropin)

            syncer.sync()

            categoriesRepo.singleShould().id shouldBe checkinJsonDropin.partner!!.category.id
        }
        "Given nothing but workout-checkin When sync Then insert checkin" {
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
        "Given nothing but dropin-checkin When sync Then insert checkin" {
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
        "Given workout exists When sync Then skip it" {
            val yetExisting = Arb.checkinEntityWorkout().next().copy(id = UUID.fromString(checkinJson.uuid))
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