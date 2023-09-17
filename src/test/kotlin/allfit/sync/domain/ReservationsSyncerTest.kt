package allfit.sync.domain

import allfit.TestDates
import allfit.api.InMemoryOnefitClient
import allfit.api.models.ReservationJson
import allfit.api.models.reservationJson
import allfit.api.models.reservationsJsonRoot
import allfit.persistence.domain.ExposedReservationsRepo
import allfit.persistence.domain.ExposedWorkoutsRepo
import allfit.persistence.domain.InMemoryReservationsRepo
import allfit.persistence.domain.InMemoryWorkoutsRepo
import allfit.persistence.domain.ReservationEntity
import allfit.persistence.testInfra.DbListener
import allfit.persistence.testInfra.ExposedTestRepo
import allfit.persistence.testInfra.reservationEntity
import allfit.service.InMemoryWorkoutInserter
import allfit.service.InsertWorkout
import allfit.service.WorkoutInserterImpl
import allfit.service.toUtcLocalDateTime
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
    private lateinit var workoutInserter: InMemoryWorkoutInserter


    override suspend fun beforeEach(testCase: TestCase) {
        client = InMemoryOnefitClient()
        reservationsRepo = InMemoryReservationsRepo()
        workoutsRepo = InMemoryWorkoutsRepo()
        workoutInserter = InMemoryWorkoutInserter()
        syncer = ReservationsSyncerImpl(
            client, reservationsRepo, clock, workoutInserter, workoutsRepo, InMemorySyncListenerManager()
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
    private val workoutFetch = Arb.workoutFetch().next()
    private val clock = TestDates.clock
    private lateinit var client: InMemoryOnefitClient
    private lateinit var syncer: ReservationsSyncer
    private lateinit var workoutFetcher: InMemoryWorkoutFetcher

    override suspend fun beforeEach(testCase: TestCase) {
        client = InMemoryOnefitClient()
        workoutFetcher = InMemoryWorkoutFetcher()
        val workoutInserter = WorkoutInserterImpl(
            ExposedWorkoutsRepo, workoutFetcher
        )
        syncer = ReservationsSyncerImpl(
            client, ExposedReservationsRepo, clock, workoutInserter, ExposedWorkoutsRepo, InMemorySyncListenerManager()
        )
    }

    init {
        extension(DbListener())

        "Given partner but no workout and reservation Then insert workout" {
            client.mockReservationsResponse(reservationJson)
            ExposedTestRepo.insertCategoryAndPartner(withPartner = { it.copy(id = reservationJson.workout.partner.id) })
            workoutFetcher.urlToFetches[WorkoutUrl(
                workoutId = reservationJson.workout.id, workoutSlug = reservationJson.workout.slug
            )] = workoutFetch

            syncer.sync()

            ExposedWorkoutsRepo.selectAll().shouldBeSingleton().first().also { workout ->
                workout.id shouldBe reservationJson.workout.id
            }
        }

        // TODO "Given no workout and no partner Then insert both"

        "Given workout and reservation Then insert reservation for that workout" {
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
