package allfit.sync.domain

import allfit.api.OnefitClient
import allfit.api.WorkoutSearchParams
import allfit.api.models.WorkoutJson
import allfit.persistence.domain.CheckinsRepository
import allfit.persistence.domain.PartnersRepo
import allfit.persistence.domain.ReservationsRepo
import allfit.persistence.domain.WorkoutsRepo
import allfit.service.Clock
import allfit.service.ImageStorage
import allfit.service.InsertWorkout
import allfit.service.WorkoutInserter
import allfit.service.toUtcLocalDateTime
import allfit.service.toWorkoutInsertListener
import allfit.sync.core.SyncListenerManager
import io.github.oshai.kotlinlogging.KotlinLogging.logger

interface WorkoutsSyncer {
    suspend fun sync()
}

class WorkoutsSyncerImpl(
    private val client: OnefitClient,
    private val workoutsRepo: WorkoutsRepo,
    private val partnersRepo: PartnersRepo,
    private val imageStorage: ImageStorage,
    private val checkinsRepository: CheckinsRepository,
    private val reservationsRepo: ReservationsRepo,
    private val clock: Clock,
    private val workoutInserter: WorkoutInserter,
    private val syncListeners: SyncListenerManager,
) : WorkoutsSyncer {

    private val log = logger {}
    private val syncDaysIntoFuture = 14

    override suspend fun sync() {
        log.debug { "Syncing workouts..." }
        val workoutsToBeSyncedJson = getWorkoutsToBeSynced()
        workoutInserter.insert(
            workoutsToBeSyncedJson.map { it.toInsertWorkout() },
            syncListeners.toWorkoutInsertListener()
        )
        deleteOutdated()
    }

    private suspend fun getWorkoutsToBeSynced(): List<WorkoutJson> {
        val from = clock.todayBeginOfDay()
        val rawClientWorkouts = client.getWorkouts(WorkoutSearchParams(from = from, plusDays = syncDaysIntoFuture)).data
        val distinctClientWorkouts = rawClientWorkouts.distinctBy { it.id }
        if (rawClientWorkouts.size != distinctClientWorkouts.size) {
            log.warn { "Dropped ${rawClientWorkouts.size - distinctClientWorkouts.size} workouts because of duplicate IDs." }
        }
        val nonExistingClientWorkoutIds = distinctClientWorkouts.map { it.id }.toMutableList()
        val existingWorkoutIds = workoutsRepo.selectAllIds()
        nonExistingClientWorkoutIds.removeAll(existingWorkoutIds)
        val nonExistingClientWorkouts = distinctClientWorkouts.filter { nonExistingClientWorkoutIds.contains(it.id) }

        // remove all workouts without an existing partner (partner seems disabled, yet workout is being returned)
        val partnerIds = partnersRepo.selectAllIds()
        return nonExistingClientWorkouts.filter { workout ->
            val partnerForWorkoutExistsLocally = partnerIds.contains(workout.partner.id)
            if (!partnerForWorkoutExistsLocally) {
                log.warn { "Dropping workout because partner is not known (set inactive by OneFit?!): $workout" }
            }
            partnerForWorkoutExistsLocally
        }
    }

    private fun deleteOutdated() {
        reservationsRepo.deleteAllBefore(clock.now().toUtcLocalDateTime())
        val workoutIdsWithCheckin = checkinsRepository.selectAll().map { it.workoutId }
        val workoutIdsToDelete = workoutsRepo.selectAllBefore(clock.todayBeginOfDay().toUtcLocalDateTime())
            .filter { !workoutIdsWithCheckin.contains(it.id) }.map { it.id }
        log.debug { "Deleting ${workoutIdsToDelete.size} outdated workouts." }
        workoutsRepo.deleteAll(workoutIdsToDelete)
        imageStorage.deleteWorkoutImages(workoutIdsToDelete)
    }
}

private fun WorkoutJson.toInsertWorkout(): InsertWorkout = InsertWorkout(
    id = id,
    partnerId = partner.id,
    slug = slug,
    name = name,
    from = from,
    till = till,
)
