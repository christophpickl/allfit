package allfit.sync.domain

import allfit.api.OnefitClient
import allfit.api.models.ReservationJson
import allfit.persistence.domain.ReservationEntity
import allfit.persistence.domain.ReservationsRepo
import allfit.persistence.domain.WorkoutsRepo
import allfit.service.Clock
import allfit.service.InsertWorkout
import allfit.service.WorkoutInserter
import allfit.service.toUtcLocalDateTime
import allfit.service.toWorkoutInsertListener
import allfit.sync.core.SyncListenerManager
import io.github.oshai.kotlinlogging.KotlinLogging.logger
import java.util.UUID

interface ReservationsSyncer {
    suspend fun sync()
}

class ReservationsSyncerImpl(
    private val client: OnefitClient,
    private val reservationsRepo: ReservationsRepo,
    private val clock: Clock,
    private val workoutInserter: WorkoutInserter,
    private val workoutRepo: WorkoutsRepo,
    private val listeners: SyncListenerManager,
) : ReservationsSyncer {
    private val log = logger {}

    override suspend fun sync() {
        log.debug { "Syncing reservations ..." }
        listeners.onSyncDetail("Syncing reservations ...")

        val reservationsRemote = client.getReservations()
        val reservationsLocal = reservationsRepo.selectAllStartingFrom(clock.now().toUtcLocalDateTime())

        val toBeInserted = reservationsRemote.data.associateBy { UUID.fromString(it.uuid) }.toMutableMap()
        reservationsLocal.forEach {
            toBeInserted.remove(it.uuid)
        }

        val toBeDeleted = reservationsLocal.associateBy { it.uuid.toString() }.toMutableMap()
        reservationsRemote.data.forEach {
            toBeDeleted.remove(it.uuid)
        }
        syncDependents(toBeInserted.values.toList())
        reservationsRepo.insertAll(toBeInserted.values.map { it.toReservationEntity() })
        reservationsRepo.deleteAll(toBeDeleted.map { UUID.fromString(it.key) })

        listeners.onSyncDetailReport(DiffReport(toBeInserted.values.toList(), toBeDeleted.values.toList()), "reservations")
    }

    private suspend fun syncDependents(reservations: List<ReservationJson>) {
        val workouts = workoutRepo.selectAllForIds(reservations.map { it.workout.id }).map { it.id }
        val workoutsToBeInserted = reservations.filter { reservation -> !workouts.contains(reservation.workout.id) }
        workoutInserter.insert(
            workoutsToBeInserted.map { it.toInsertWorkout() },
            listeners.toWorkoutInsertListener()
        )
    }
}

private fun ReservationJson.toInsertWorkout(): InsertWorkout = InsertWorkout(
    id = workout.id,
    partnerId = workout.partner.id,
    slug = workout.slug,
    name = workout.name,
    from = workout.from,
    till = workout.till,
)

private fun ReservationJson.toReservationEntity() = ReservationEntity(
    uuid = UUID.fromString(uuid),
    workoutId = workout.id,
    workoutStart = workout.from.toUtcLocalDateTime(),
)
