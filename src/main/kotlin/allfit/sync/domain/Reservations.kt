package allfit.sync.domain

import allfit.api.OnefitClient
import allfit.api.models.ReservationJson
import allfit.api.models.WorkoutReservationPartnerJson
import allfit.persistence.domain.*
import allfit.service.*
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
    private val partnerInserter: PartnerInserter,
    private val categoriesRepo: CategoriesRepo,
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
        syncPartners(workoutsToBeInserted)

        workoutInserter.insert(
            workoutsToBeInserted.map { it.toInsertWorkout() },
            listeners.toWorkoutInsertListener()
        )
    }

    private suspend fun syncPartners(workoutsToBeInserted: List<ReservationJson>) {
        val existingPartnerIds = partnerInserter.selectAllIds().toSet()
        val partnersToInsert = workoutsToBeInserted
            .filter { !existingPartnerIds.contains(it.workout.partner.id) }
            // TODO simplify
//            .map { it.workout.partner }
//            .distinctBy { it.id }
            .associate { it.workout.partner.id to it.workout.partner }
            .map { it.value }

        val existingCategoryIds = categoriesRepo.selectAll().map { it.id }.toSet()
        val categoriesToInsert = partnersToInsert.map { it.category }.distinctBy { it.id }.filter { !existingCategoryIds.contains(it.id) }

        categoriesRepo.insertAll(categoriesToInsert.map { it.toCategoryEntity() })
        partnerInserter.insertAllWithImage(partnersToInsert.map { it.toPartnerEntity() })
    }
}

private fun WorkoutReservationPartnerJson.toPartnerEntity() =
    PartnerEntity(
        id = id,
        primaryCategoryId = category.id,
        secondaryCategoryIds = emptyList(),
        name = name,
        slug = slug,
        description = "No description available as this partner was not on the main database but was synced via a reserved workout -most probably outside of your configured location?",
        facilities = "",
        imageUrl = header_image?.orig,
        hasDropins = null,
        hasWorkouts = null,

        // custom fields:
        note = "",
        officialWebsite = null,
        rating = 0,
        isDeleted = false,
        isFavorited = false,
        isHidden = false,
        isWishlisted = false,
        locationShortCode = "???",
    )

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
