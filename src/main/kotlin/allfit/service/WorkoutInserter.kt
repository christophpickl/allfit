package allfit.service

import allfit.persistence.domain.WorkoutEntity
import allfit.persistence.domain.WorkoutsRepo
import allfit.sync.core.SyncListenerManager
import allfit.sync.domain.WorkoutFetchMetadata
import allfit.sync.domain.WorkoutMetadata
import allfit.sync.domain.WorkoutMetadataFetchListener
import allfit.sync.domain.WorkoutMetadataFetcher
import allfit.sync.domain.WorkoutUrl
import io.github.oshai.kotlinlogging.KotlinLogging.logger
import java.time.ZonedDateTime
import kotlin.time.Duration.Companion.milliseconds
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

class WorkoutInserterImpl(
    private val workoutsRepo: WorkoutsRepo,
    private val metadataFetcher: WorkoutMetadataFetcher,
) : WorkoutInserter {

    private val log = logger {}
    private val numberOfParallelFetchers = 10
    private val pauseBetweenEachFetch = 100.milliseconds

    override suspend fun insert(workouts: List<InsertWorkout>, listener: WorkoutInsertListener) {
        val metaFetchById = fetchMetaData(workouts, listener)

        listener.onProgress("Inserting ${workouts.size} workouts into database.")
        workoutsRepo.insertAll(workouts.map { it.toWorkoutEntity(metaFetchById[it.id]!!) })
    }

    private suspend fun fetchMetaData(
        workoutsToBeSyncedJson: List<InsertWorkout>,
        listener: WorkoutInsertListener
    ): Map<Int, WorkoutFetchMetadata> {
        log.debug { "Fetching metadata for ${workoutsToBeSyncedJson.size} workouts." }
        listener.onProgress("Fetching metadata for ${workoutsToBeSyncedJson.size} workouts.")
        val metaFetchById = mutableMapOf<Int, WorkoutFetchMetadata>()
        val fetchListener = listener.toWorkoutMetadataFetchListener()
        workoutsToBeSyncedJson.workParallel(
            numberOfCoroutines = numberOfParallelFetchers,
            percentageBroadcastIntervalInMs = 12_000,
            percentageProgressCallback = {
                listener.onProgress("Fetched ${(it * 100).toInt()}% of workout metadata.")
            }) { workout ->
            metaFetchById[workout.id] = metadataFetcher.fetch(
                WorkoutUrl(workoutId = workout.id, workoutSlug = workout.slug), fetchListener
            )
            delay(pauseBetweenEachFetch) // artificial delay to soothe possible cloudflare's DoS anger :)
        }
        return metaFetchById
    }

    private fun WorkoutInsertListener.toWorkoutMetadataFetchListener() = object : WorkoutMetadataFetchListener {
        override fun failedFetching(message: String) {
            onProgress("⚠️ $message")
        }
    }
}

private fun InsertWorkout.toWorkoutEntity(htmlMetaData: WorkoutMetadata) = WorkoutEntity(
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
