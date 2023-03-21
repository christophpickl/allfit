package allfit.sync

import allfit.api.OnefitClient
import allfit.api.models.ReservationJson
import allfit.persistence.ReservationEntity
import allfit.persistence.ReservationsRepo
import allfit.service.SystemClock
import allfit.service.toUtcLocalDateTime
import mu.KotlinLogging.logger
import java.util.UUID

interface ReservationsSyncer {
    suspend fun sync()
}

class ReservationsSyncerImpl(
    private val client: OnefitClient,
    private val reservationsRepo: ReservationsRepo,
) : ReservationsSyncer {
    private val log = logger {}

    override suspend fun sync() {
        log.debug { "Syncing reservations..." }
        val reservationsRemote = client.getReservations()
        val reservationsLocal = reservationsRepo.selectAllStartingFrom(SystemClock.now().toUtcLocalDateTime())

        val toBeInserted = reservationsRemote.data.associateBy { UUID.fromString(it.uuid) }.toMutableMap()
        reservationsLocal.forEach {
            toBeInserted.remove(it.uuid)
        }

        val toBeDeleted = reservationsLocal.associateBy { it.uuid.toString() }.toMutableMap()
        reservationsRemote.data.forEach {
            toBeDeleted.remove(it.uuid)
        }
        reservationsRepo.insertAll(toBeInserted.values.map { it.toReservationEntity() })
        reservationsRepo.deleteAll(toBeDeleted.map { UUID.fromString(it.key) })
    }
}

private fun ReservationJson.toReservationEntity() = ReservationEntity(
    uuid = UUID.fromString(uuid),
    workoutId = workout.id,
    workoutStart = workout.from.toUtcLocalDateTime(),
)
