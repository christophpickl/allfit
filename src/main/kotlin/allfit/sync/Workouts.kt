package allfit.sync

import allfit.api.OnefitClient
import allfit.api.WorkoutSearchParams
import allfit.api.models.WorkoutJson
import allfit.persistence.PartnersRepo
import allfit.persistence.WorkoutEntity
import allfit.persistence.WorkoutsRepo
import allfit.service.ImageStorage
import allfit.service.SystemClock
import allfit.service.WorkoutAndImageUrl
import allfit.service.toUtcLocalDateTime
import mu.KotlinLogging.logger

class WorkoutsSyncer(
    private val client: OnefitClient,
    private val workoutsRepo: WorkoutsRepo,
    private val workoutFetcher: WorkoutFetcher,
    private val partnersRepo: PartnersRepo,
    private val imageStorage: ImageStorage,
) {
    private val log = logger {}

    suspend fun sync() {
        log.debug { "Syncing workouts..." }
        val workoutsToBeSyncedJson = getWorkoutsToBeSynced()
        val metaFetchById = workoutsToBeSyncedJson.map {
            workoutFetcher.fetch(WorkoutUrl(workoutId = it.id, workoutSlug = it.slug))
        }.associateBy { it.workoutId }
        workoutsRepo.insertAll(workoutsToBeSyncedJson.map { it.toWorkoutEntity(metaFetchById[it.id]!!) })
        imageStorage.saveWorkoutImages(metaFetchById.values
            .filter { it.imageUrls.isNotEmpty() }
            .map { WorkoutAndImageUrl(it.workoutId, it.imageUrls.first()) })

        // FIXME delete workouts before today which has no association with a reservation; and also images!
    }

    private suspend fun getWorkoutsToBeSynced(): List<WorkoutJson> {
        val from = SystemClock.todayBeginOfDay()
        val workouts = client.getWorkouts(WorkoutSearchParams.simple(from = from, plusDays = 14)).data
        val workoutIdsToBeInserted = workouts.map { it.id }.toMutableList()
        val entities = workoutsRepo.selectAllStartingFrom(from.toUtcLocalDateTime())
        entities.forEach {
            workoutIdsToBeInserted.remove(it.id)
        }
        val maybeInsert = workouts.filter { workoutIdsToBeInserted.contains(it.id) }

        // remove all workouts without an existing partner (partner seems disabled, yet workout is being returned)
        val partnerIds = partnersRepo.selectAll().map { it.id }
        return maybeInsert.filter {
            val existing = partnerIds.contains(it.partner.id)
            if (!existing) {
                log.warn { "Dropping workout because partner is not known (set inactive by OneFit?!): $it" }
            }
            existing
        }
    }
}

interface WorkoutHtmlMetaData {
    val about: String
    val specifics: String
    val address: String
}

private fun WorkoutJson.toWorkoutEntity(htmlMetaData: WorkoutHtmlMetaData) = WorkoutEntity(
    id = id,
    name = name,
    slug = slug,
    start = from.toUtcLocalDateTime(),
    end = till.toUtcLocalDateTime(),
    partnerId = partner.id,
    about = htmlMetaData.about,
    specifics = htmlMetaData.specifics,
    address = htmlMetaData.address,
)
