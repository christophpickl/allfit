package allfit.sync

import allfit.api.InMemoryOnefitClient
import allfit.api.models.WorkoutJson
import allfit.api.models.workoutJson
import allfit.api.models.workoutPartnerJson
import allfit.api.models.workoutsJson
import allfit.persistence.InMemoryPartnersRepo
import allfit.persistence.InMemoryWorkoutsRepo
import allfit.persistence.WorkoutEntity
import allfit.persistence.partnerEntity
import allfit.persistence.workoutEntity
import allfit.service.InMemoryImageStorage
import allfit.service.WorkoutAndImageUrl
import allfit.service.toUtcLocalDateTime
import io.kotest.core.spec.style.StringSpec
import io.kotest.core.test.TestCase
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldBeSingleton
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.maps.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next

class WorkoutsSyncerTest : StringSpec() {

    private val partnerEntity = Arb.partnerEntity().next()
    private val workoutJson = Arb.workoutJson().next()
    private val workoutFetch = Arb.workoutFetch().next()
    private val workoutEntity = Arb.workoutEntity().next()
    private lateinit var syncer: WorkoutsSyncer
    private lateinit var client: InMemoryOnefitClient
    private lateinit var workoutFetcher: DummyWorkoutFetcher
    private lateinit var workoutsRepo: InMemoryWorkoutsRepo
    private lateinit var partnersRepo: InMemoryPartnersRepo
    private lateinit var imageStorage: InMemoryImageStorage

    override suspend fun beforeEach(testCase: TestCase) {
        client = InMemoryOnefitClient()
        workoutsRepo = InMemoryWorkoutsRepo()
        workoutFetcher = DummyWorkoutFetcher()
        partnersRepo = InMemoryPartnersRepo()
        imageStorage = InMemoryImageStorage()
        syncer = WorkoutsSyncer(client, workoutsRepo, workoutFetcher, partnersRepo, imageStorage)
    }

    private fun workoutWithPartnerId(partnerId: Int) = Arb.workoutJson().next()
        .copy(partner = Arb.workoutPartnerJson().next().copy(id = partnerId))

    private fun insertPartnerForWorkout(): WorkoutJson {
        partnersRepo.insertAll(listOf(partnerEntity))
        return workoutWithPartnerId(partnerEntity.id)
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
        "Given workout already in DB Then do nothing" {
            workoutsRepo.insertAll(listOf(workoutEntity))
            val workoutJson = Arb.workoutJson().next().copy(id = workoutEntity.id)
            client.mockWorkoutsResponse(workoutJson)

            syncer.sync()

            workoutsRepo.selectAllStartingFrom(workoutJson.from.toUtcLocalDateTime()).shouldBeSingleton()
                .first() shouldBe workoutEntity
        }
    }
}

private fun InMemoryOnefitClient.mockWorkoutsResponse(vararg workouts: WorkoutJson) {
    workoutsJson = Arb.workoutsJson().next().copy(data = workouts.toList())
}
