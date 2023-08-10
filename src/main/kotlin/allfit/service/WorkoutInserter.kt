package allfit.service

import allfit.persistence.domain.WorkoutEntity
import allfit.persistence.domain.WorkoutsRepo
import allfit.sync.core.SyncListenerManager
import allfit.sync.domain.WorkoutFetch
import allfit.sync.domain.WorkoutFetcher
import allfit.sync.domain.WorkoutHtmlMetaData
import allfit.sync.domain.WorkoutUrl
import io.github.oshai.kotlinlogging.KotlinLogging.logger
import java.time.ZonedDateTime
import kotlinx.coroutines.delay

data class InsertWorkout(
    val id: Int,
    val partnerId: Int,
    val name: String,
    val slug: String,
    val from: ZonedDateTime,
    val till: ZonedDateTime,
)

interface WorkoutInserter {
    suspend fun insert(workouts: List<InsertWorkout>, listener: WorkoutInsertListener)
}

interface WorkoutInsertListener {
    fun onProgress(message: String)
}

// FIXME write test
class WorkoutInserterImpl(
    private val workoutsRepo: WorkoutsRepo,
    private val workoutFetcher: WorkoutFetcher,
    private val imageStorage: ImageStorage,
) : WorkoutInserter {

    private val log = logger {}

    private val parallelFetchers = 6

    override suspend fun insert(workouts: List<InsertWorkout>, listener: WorkoutInsertListener) {
        val metaFetchById = fetchMetaData(workouts, listener)

        listener.onProgress("Inserting ${workouts.size} workouts into DB.")
        workoutsRepo.insertAll(workouts.map { it.toWorkoutEntity(metaFetchById[it.id]!!) })
        val workoutsFetchImages = metaFetchById.values
            .filter { it.imageUrls.isNotEmpty() }
            .map { WorkoutAndImageUrl(it.workoutId, it.imageUrls.first()) }

        listener.onProgress("Downloading ${workoutsFetchImages.size} workout images.")
        imageStorage.downloadWorkoutImages(workoutsFetchImages)
    }

    private suspend fun fetchMetaData(
        workoutsToBeSyncedJson: List<InsertWorkout>,
        listener: WorkoutInsertListener
    ): Map<Int, WorkoutFetch> {
        log.debug { "Fetching metadata for ${workoutsToBeSyncedJson.size} workouts." }
        listener.onProgress("Fetching metadata for ${workoutsToBeSyncedJson.size} workouts.")
        val metaFetchById = mutableMapOf<Int, WorkoutFetch>()
        workoutsToBeSyncedJson.workParallel(
            numberOfCoroutines = parallelFetchers,
            percentageBroadcastIntervalInMs = 12_000,
            percentageProgressCallback = {
                listener.onProgress("Fetched ${(it * 100).toInt()}% of workout metadata.")
            }) { workout ->
            metaFetchById[workout.id] = workoutFetcher.fetch(
                WorkoutUrl(
                    workoutId = workout.id,
                    workoutSlug = workout.slug
                )
            )
            delay(30) // artificial delay to soothen possible cloudflare's DoS anger :)
        }
        return metaFetchById
    }
}

private fun InsertWorkout.toWorkoutEntity(htmlMetaData: WorkoutHtmlMetaData) = WorkoutEntity(
    id = id,
    partnerId = partnerId,
    name = name,
    slug = slug,
    start = from.toUtcLocalDateTime(),
    end = till.toUtcLocalDateTime(),
    about = htmlMetaData.about,
    specifics = htmlMetaData.specifics,
    address = htmlMetaData.address,
    teacher = htmlMetaData.teacher,
)

fun SyncListenerManager.toWorkoutInsertListener() = object : WorkoutInsertListener {
    override fun onProgress(message: String) {
        this@toWorkoutInsertListener.onSyncDetail(message)
    }
}
