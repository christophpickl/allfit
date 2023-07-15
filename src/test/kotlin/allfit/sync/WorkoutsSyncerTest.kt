package allfit.sync

import allfit.TestDates
import allfit.api.InMemoryOnefitClient
import allfit.api.models.WorkoutJson
import allfit.api.models.workoutJson
import allfit.api.models.workoutPartnerJson
import allfit.api.models.workoutsJsonRoot
import allfit.persistence.domain.CheckinsRepository
import allfit.persistence.domain.InMemoryCheckinsRepository
import allfit.persistence.domain.InMemoryPartnersRepo
import allfit.persistence.domain.InMemoryReservationsRepo
import allfit.persistence.domain.InMemoryWorkoutsRepo
import allfit.persistence.domain.WorkoutEntity
import allfit.persistence.testInfra.checkinEntityWorkout
import allfit.persistence.testInfra.partnerEntity
import allfit.persistence.testInfra.reservationEntity
import allfit.persistence.testInfra.singletonShouldBe
import allfit.persistence.testInfra.workoutEntity
import allfit.service.InMemoryImageStorage
import allfit.service.WorkoutAndImageUrl
import allfit.service.toUtcLocalDateTime
import allfit.sync.core.SyncListenerManagerImpl
import allfit.sync.domain.DummyWorkoutFetcher
import allfit.sync.domain.WorkoutsSyncer
import allfit.sync.domain.WorkoutsSyncerImpl
import io.kotest.core.spec.style.StringSpec
import io.kotest.core.test.TestCase
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldBeSingleton
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.maps.shouldBeEmpty
import io.kotest.matchers.maps.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next

class WorkoutsSyncerTest : StringSpec() {

    private val partnerEntity = Arb.partnerEntity().next()
    private val workoutJson = Arb.workoutJson().next()
    private val workoutFetch = Arb.workoutFetch().next()
    private val workoutEntity = Arb.workoutEntity().next()
    private val pastDateTime = TestDates.now.minusYears(1)
    private val clock = TestDates.clock
    private lateinit var syncer: WorkoutsSyncer
    private lateinit var client: InMemoryOnefitClient
    private lateinit var workoutFetcher: DummyWorkoutFetcher
    private lateinit var workoutsRepo: InMemoryWorkoutsRepo
    private lateinit var partnersRepo: InMemoryPartnersRepo
    private lateinit var imageStorage: InMemoryImageStorage
    private lateinit var checkinsRepository: CheckinsRepository
    private lateinit var reservationsRepo: InMemoryReservationsRepo

    override suspend fun beforeEach(testCase: TestCase) {
        client = InMemoryOnefitClient()
        workoutsRepo = InMemoryWorkoutsRepo()
        workoutFetcher = DummyWorkoutFetcher()
        partnersRepo = InMemoryPartnersRepo()
        imageStorage = InMemoryImageStorage()
        checkinsRepository = InMemoryCheckinsRepository()
        reservationsRepo = InMemoryReservationsRepo()
        syncer = WorkoutsSyncerImpl(
            client,
            workoutsRepo,
            workoutFetcher,
            partnersRepo,
            imageStorage,
            checkinsRepository,
            reservationsRepo,
            SyncListenerManagerImpl(),
            clock,
        )
    }

    init {
        "Given partner stored Then insert workout" {
            val workout = insertPartnerForWorkout()
            client.mockWorkoutsResponse(workout)
            workoutFetcher.fetched = workoutFetch

            syncer.sync()

            workoutsRepo.selectAllStartingFrom(workout.from.toUtcLocalDateTime()).shouldBeSingleton()
                .first() shouldBe WorkoutEntity(
                id = workout.id,
                partnerId = workout.partner.id,
                name = workout.name,
                slug = workout.slug,
                start = workout.from.toUtcLocalDateTime(),
                end = workout.till.toUtcLocalDateTime(),
                about = workoutFetch.about,
                specifics = workoutFetch.specifics,
                teacher = workoutFetch.teacher,
                address = workoutFetch.address,
            )
        }
        "Given fetched image Then save it" {
            val workout = insertPartnerForWorkout()
            client.mockWorkoutsResponse(workout)
            val workoutFetchWithImage = workoutFetch.copy(imageUrls = listOf("url"))
            workoutFetcher.fetched = workoutFetchWithImage

            syncer.sync()

            imageStorage.savedWorkoutImages shouldContainExactly listOf(WorkoutAndImageUrl(workout.id, "url"))
        }
        "Given no partner Then ignore workout" {
            client.mockWorkoutsResponse(workoutJson)

            syncer.sync()

            workoutsRepo.workouts.shouldBeEmpty()
            imageStorage.savedPartnerImages.shouldBeEmpty()
        }
        "Given duplicate workouts Then filter them out" {
            partnersRepo.insertAll(listOf(partnerEntity))
            val partnerJson = Arb.workoutPartnerJson().next().copy(id = partnerEntity.id)
            val workoutJson1 = Arb.workoutJson().next().copy(id = 1, partner = partnerJson)
            val workoutJson2 = Arb.workoutJson().next().copy(id = 1, partner = partnerJson)
            client.mockWorkoutsResponse(workoutJson1, workoutJson2)

            syncer.sync()

            workoutsRepo.workouts shouldHaveSize 1
        }
        "Given workout already in DB Then do nothing" {
            workoutsRepo.insertAll(listOf(workoutEntity))
            val workoutJson = Arb.workoutJson().next().copy(id = workoutEntity.id)
            client.mockWorkoutsResponse(workoutJson)

            syncer.sync()

            workoutsRepo.selectAllStartingFrom(workoutJson.from.toUtcLocalDateTime()).shouldBeSingleton()
                .first() shouldBe workoutEntity
        }
        "Given past reservation When sync Then delete it" {
            reservationsRepo.insertAll(listOf(Arb.reservationEntity().next().copy(workoutStart = pastDateTime)))

            syncer.sync()

            reservationsRepo.reservations.shouldBeEmpty()
        }
        "Given visited workout without association When sync Then delete it and remove image" {
            val visitedWorkout = workoutEntity.copy(start = pastDateTime)
            workoutsRepo.insertAll(listOf(visitedWorkout))

            syncer.sync()

            workoutsRepo.workouts.shouldBeEmpty()
            imageStorage.deletedWorkoutImages.shouldBeSingleton().first() shouldBe visitedWorkout.id
        }
        "Given visited workout with association When sync Then keep it" {
            val visitedWorkout = workoutEntity.copy(start = pastDateTime)
            checkinsRepository.insertAll(listOf(Arb.checkinEntityWorkout().next().copy(workoutId = visitedWorkout.id)))
            workoutsRepo.insertAll(listOf(visitedWorkout))

            syncer.sync()

            workoutsRepo singletonShouldBe visitedWorkout
        }
    }

    private fun insertPartnerForWorkout(): WorkoutJson {
        partnersRepo.insertAll(listOf(partnerEntity))
        return workoutWithPartnerId(partnerEntity.id)
    }
}

private fun workoutWithPartnerId(partnerId: Int) = Arb.workoutJson().next()
    .copy(partner = Arb.workoutPartnerJson().next().copy(id = partnerId))

private fun InMemoryOnefitClient.mockWorkoutsResponse(vararg workouts: WorkoutJson) {
    workoutsJson = Arb.workoutsJsonRoot().next().copy(data = workouts.toList())
}
