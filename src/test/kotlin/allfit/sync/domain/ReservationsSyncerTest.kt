package allfit.sync.domain

import allfit.TestDates
import allfit.api.InMemoryOnefitClient
import allfit.api.models.ReservationJson
import allfit.api.models.WorkoutReservationJson
import allfit.api.models.reservationJson
import allfit.api.models.reservationsJsonRoot
import allfit.persistence.domain.*
import allfit.persistence.testInfra.DbListener
import allfit.persistence.testInfra.ExposedTestRepo
import allfit.persistence.testInfra.reservationEntity
import allfit.service.*
import allfit.sync.core.InMemorySyncListenerManager
import io.kotest.core.spec.style.StringSpec
import io.kotest.core.test.TestCase
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldBeSingleton
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import java.util.UUID

class ReservationsSyncerTest : StringSpec() {

    private val reservationEntity = Arb.reservationEntity().next()
    private val reservationJson = Arb.reservationJson().next()

    private val clock = TestDates.clock

    private lateinit var syncer: ReservationsSyncer
    private lateinit var client: InMemoryOnefitClient
    private lateinit var reservationsRepo: InMemoryReservationsRepo
    private lateinit var workoutsRepo: InMemoryWorkoutsRepo
    private lateinit var categoriesRepo: InMemoryCategoriesRepo
    private lateinit var workoutInserter: InMemoryWorkoutInserter
    private lateinit var partnerInserter: InMemoryPartnerInserter


    override suspend fun beforeEach(testCase: TestCase) {
        client = InMemoryOnefitClient()
        reservationsRepo = InMemoryReservationsRepo()
        workoutsRepo = InMemoryWorkoutsRepo()
        categoriesRepo = InMemoryCategoriesRepo()
        workoutInserter = InMemoryWorkoutInserter()
        partnerInserter = InMemoryPartnerInserter()
        syncer = ReservationsSyncerImpl(
            client,
            reservationsRepo,
            clock,
            workoutInserter,
            partnerInserter,
            categoriesRepo,
            workoutsRepo,
            InMemorySyncListenerManager()
        )
    }

    init {
        "Given client reservation Then insert reservation" {
            client.mockReservationsResponse(reservationJson)

            syncer.sync()

            reservationsRepo.selectAll().shouldBeSingleton().first() shouldBe ReservationEntity(
                uuid = UUID.fromString(reservationJson.uuid),
                workoutId = reservationJson.workout.id,
                workoutStart = reservationJson.workout.from.toUtcLocalDateTime(),
            )
        }

        "Given client reservation Then insert workout" {
            client.mockReservationsResponse(reservationJson)

            syncer.sync()

            workoutInserter.workoutsInserted.shouldBeSingleton().first().shouldBeSingleton()
                .first() shouldBe InsertWorkout(
                id = reservationJson.workout.id,
                partnerId = reservationJson.workout.partner.id,
                name = reservationJson.workout.name,
                slug = reservationJson.workout.slug,
                from = reservationJson.workout.from,
                till = reservationJson.workout.till,
            )
        }

        "Given reservation already exists Then ignore it" {
            reservationsRepo.insertAll(listOf(reservationEntity))
            client.mockReservationsResponse(reservationJson.copy(uuid = reservationEntity.uuid.toString()))

            syncer.sync()

            reservationsRepo.selectAll().shouldBeSingleton().first().uuid shouldBe reservationEntity.uuid
        }

        "Given reservation not existing locally Then delete it" {
            reservationsRepo.insertAll(listOf(reservationEntity))

            syncer.sync()

            reservationsRepo.selectAll().shouldBeEmpty()
        }
    }
}

class ReservationsSyncerIntegrationTest : StringSpec() {

    private val reservationJson = Arb.reservationJson().next()
    private val workoutFetch: WorkoutFetchMetadata = Arb.workoutFetch().next()
    private val clock = TestDates.clock
    private lateinit var client: InMemoryOnefitClient
    private lateinit var syncer: ReservationsSyncer
    private lateinit var workoutFetcher: InMemoryWorkoutFetcher
    private lateinit var imageStorage: ImageStorage

    override suspend fun beforeEach(testCase: TestCase) {
        client = InMemoryOnefitClient()
        workoutFetcher = InMemoryWorkoutFetcher()
        imageStorage = InMemoryImageStorage()
        syncer = ReservationsSyncerImpl(
            client,
            ExposedReservationsRepo,
            clock,
            WorkoutInserterImpl(ExposedWorkoutsRepo, workoutFetcher),
            PartnerInserterImpl(ExposedPartnersRepo, imageStorage),
            ExposedCategoriesRepo,
            ExposedWorkoutsRepo,
            InMemorySyncListenerManager()
        )
    }

    private fun mockWorkoutMetadataFetching(workout: WorkoutReservationJson, metadata: WorkoutFetchMetadata) {
        workoutFetcher.urlToFetches[WorkoutUrl(
            workoutId = workout.id, workoutSlug = workout.slug
        )] = metadata
    }

    init {
        extension(DbListener())

        "Given partner but no workout When sync reservation Then insert workout" {
            client.mockReservationsResponse(reservationJson)
            ExposedTestRepo.insertCategoryAndPartner(withPartner = { it.copy(id = reservationJson.workout.partner.id) })
            mockWorkoutMetadataFetching(reservationJson.workout, workoutFetch)

            syncer.sync()

            ExposedWorkoutsRepo.selectAll().shouldBeSingleton().first().also { workout ->
                workout.id shouldBe reservationJson.workout.id
            }
        }

        "Given no workout and no partner and no category Then insert partner and category" {
            client.mockReservationsResponse(reservationJson)
            mockWorkoutMetadataFetching(reservationJson.workout, workoutFetch)

            syncer.sync()

            ExposedPartnersRepo.selectAll().shouldBeSingleton().first().also { partner ->
                partner.id shouldBe reservationJson.workout.partner.id
            }
            ExposedCategoriesRepo.selectAll().shouldBeSingleton().first().also { category ->
                category.id shouldBe reservationJson.workout.partner.category.id
            }
        }

        "Given partner, workout When sync reservation for that workout Then insert reservation for it" {
            val (_, _, workout) = ExposedTestRepo.insertCategoryPartnerAndWorkout()
            client.mockReservationsResponse(reservationJson.copy(workout = reservationJson.workout.copy(id = workout.id)))

            syncer.sync()

            ExposedReservationsRepo.selectAll().shouldBeSingleton().first().also { reservation ->
                reservation.workoutId shouldBe workout.id
            }
        }
    }
}

private fun InMemoryOnefitClient.mockReservationsResponse(vararg reservations: ReservationJson) {
    reservationsJson = Arb.reservationsJsonRoot().next().copy(data = reservations.toList())
}
