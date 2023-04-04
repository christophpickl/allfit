package allfit.service

import allfit.sync.SyncListenerManager
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.delay
import mu.KotlinLogging.logger
import java.io.File

interface ImageStorage {

    suspend fun savePartnerImages(partners: List<PartnerAndImageUrl>)
    fun saveDefaultImageForPartner(partnerIds: List<Int>)
    fun loadPartnerImages(partnerIds: List<Int>): List<PartnerAndImageBytes>

    suspend fun saveWorkoutImages(workouts: List<WorkoutAndImageUrl>)
    fun saveDefaultImageForWorkout(workoutIds: List<Int>)
    fun loadWorkoutImages(workoutIds: List<Int>): List<WorkoutAndImagesBytes>
    fun deleteWorkoutImages(workoutIds: List<Int>)
}

object DummyImageStorage : ImageStorage {

    override fun loadWorkoutImages(workoutIds: List<Int>): List<WorkoutAndImagesBytes> {
        return workoutIds.map { WorkoutAndImagesBytes(it, byteArrayOf()) }
    }

    override fun loadPartnerImages(partnerIds: List<Int>): List<PartnerAndImageBytes> {
        return partnerIds.map { PartnerAndImageBytes(it, byteArrayOf()) }
    }

    override suspend fun savePartnerImages(partners: List<PartnerAndImageUrl>) {
    }

    override fun saveDefaultImageForPartner(partnerIds: List<Int>) {
    }

    override suspend fun saveWorkoutImages(workouts: List<WorkoutAndImageUrl>) {
    }

    override fun saveDefaultImageForWorkout(workoutIds: List<Int>) {
    }

    override fun deleteWorkoutImages(workoutIds: List<Int>) {
    }
}

class InMemoryImageStorage : ImageStorage {
    val savedPartnerImages = mutableListOf<PartnerAndImageUrl>()
    val partnerImagesToBeLoaded = mutableMapOf<Int, PartnerAndImageBytes>()
    val savedWorkoutImages = mutableListOf<WorkoutAndImageUrl>()
    val workoutImagesToBeLoaded = mutableMapOf<Int, WorkoutAndImagesBytes>()
    val deletedWorkoutImages = mutableListOf<Int>()

    fun addWorkoutImagesToBeLoaded(workout: WorkoutAndImagesBytes) {
        workoutImagesToBeLoaded[workout.workoutId] = workout
    }

    fun addPartnerImagesToBeLoaded(partner: PartnerAndImageBytes) {
        partnerImagesToBeLoaded[partner.partnerId] = partner
    }

    override suspend fun savePartnerImages(partners: List<PartnerAndImageUrl>) {
        savedPartnerImages += partners
    }

    override fun saveDefaultImageForPartner(partnerIds: List<Int>) {
        savedPartnerImages += partnerIds.map { PartnerAndImageUrl(it, it.toString()) }
    }

    override fun loadPartnerImages(partnerIds: List<Int>): List<PartnerAndImageBytes> =
        partnerIds.mapNotNull {
            partnerImagesToBeLoaded[it]
        }

    override suspend fun saveWorkoutImages(workouts: List<WorkoutAndImageUrl>) {
        savedWorkoutImages += workouts
    }

    override fun saveDefaultImageForWorkout(workoutIds: List<Int>) {
        savedWorkoutImages += workoutIds.map { WorkoutAndImageUrl(it, it.toString()) }
    }

    override fun loadWorkoutImages(workoutIds: List<Int>): List<WorkoutAndImagesBytes> =
        workoutIds.mapNotNull {
            workoutImagesToBeLoaded[it]
        }

    override fun deleteWorkoutImages(workoutIds: List<Int>) {
        deletedWorkoutImages += workoutIds
    }
}

class FileSystemImageStorage(
    private val partnersFolder: File,
    private val workoutsFolder: File,
    private val syncListeners: SyncListenerManager,
) : ImageStorage {

    private val log = logger {}
    private val client = HttpClient()
    private val width = 300
    private val extension = "jpg"
    private val parallelWorkersCount = 3
    private val delayBetweenEachDownloadInMs = 40L

    override suspend fun savePartnerImages(partners: List<PartnerAndImageUrl>) {
        log.debug { "Saving ${partners.size} partner images." }
        partners.map { it.imageUrl }.requireAllEndsWithExtension(extension)
        partners.workParallel(parallelWorkersCount) {
            val bytes = client.getBytes("${it.imageUrl}?w=$width")
            partnerTarget(it.partnerId).saveAndLog(bytes)
            delay(delayBetweenEachDownloadInMs)
        }
    }

    override fun saveDefaultImageForPartner(partnerIds: List<Int>) {
        partnerIds.forEach { id ->
            partnerTarget(id).saveAndLog(null)
        }
    }

    private fun partnerTarget(partnerId: Int) = File(partnersFolder, "$partnerId.$extension")

    override fun loadPartnerImages(partnerIds: List<Int>): List<PartnerAndImageBytes> {
        log.debug { "Loading ${partnerIds.size} partner images." }
        return partnerIds.map { id ->
            val file = File(partnersFolder, "$id.$extension")
            require(file.exists()) { "Image not found for partner $id at location: ${file.absolutePath}" }
            PartnerAndImageBytes(id, file.readBytes())
        }
    }

    override suspend fun saveWorkoutImages(workouts: List<WorkoutAndImageUrl>) {
        log.debug { "Saving ${workouts.size} workout images." }
        workouts.map { it.imageUrl }.requireAllEndsWithExtension(extension)

        workouts.workParallel(parallelWorkersCount, {
            syncListeners.onSyncDetail("Saving ${(it * 100).toInt()}% of workout images done.")
        }) { workout ->
            val bytes = client.getBytes("${workout.imageUrl}?w=$width")
            workoutTarget(workout.workoutId).saveAndLog(bytes)
            delay(delayBetweenEachDownloadInMs)
        }
    }

    override fun saveDefaultImageForWorkout(workoutIds: List<Int>) {
        workoutIds.forEach { id ->
            workoutTarget(id).saveAndLog(null)
        }
    }

    private fun workoutTarget(workoutId: Int) = File(workoutsFolder, "$workoutId.$extension")

    override fun loadWorkoutImages(workoutIds: List<Int>): List<WorkoutAndImagesBytes> {
        log.debug { "Loading ${workoutIds.size} workout images." }
        return workoutIds.map { id ->
            WorkoutAndImagesBytes(
                workoutId = id,
                imageBytes = loadImageBytesFor(id),
            )
        }
    }

    private fun loadImageBytesFor(workoutId: Int): ByteArray {
        val prefix = "$workoutId-"
        return workoutsFolder.list { _, name ->
            name.startsWith(prefix)
        }!!.first().let {
            val image = File(workoutsFolder, it)
            image.readBytes()
        }
    }

    override fun deleteWorkoutImages(workoutIds: List<Int>) {
        val prefixes = workoutIds.map { "$it-" }
        workoutsFolder.list { _, name ->
            prefixes.any { name.startsWith(it) }
        }!!.forEach {
            val file = File(workoutsFolder, it)
            if (!file.delete()) {
                log.warn { "Unable to delete workout image located at: ${file.absolutePath}" }
            }
        }
    }
}

private fun List<String>.requireAllEndsWithExtension(extension: String) {
    forEach {
        if (!it.endsWith(".$extension")) {
            error("Invalid image URL (must end with '.$extension'): ${it}")
        }
    }
}

private val log = logger {}

private val notFoundDefaultImage: ByteArray =
    ImageStorage::class.java.classLoader
        .getResourceAsStream("images/not_found_default_image.jpg")?.readAllBytes()
        ?: error("Classpath image 'not_found_default_image.jpg' not found!")

private suspend fun HttpClient.getBytes(url: String): ByteArray? {
    val response = get(url)
    if (response.status == HttpStatusCode.NotFound) {
        log.warn { "Received 404 for $url" }
        return null
    }
    response.requireOk()
    return response.body<ByteArray>()
}

private fun File.saveAndLog(imageBinaryData: ByteArray?) {
    if (exists()) {
        log.warn { "Overwriting image file $absolutePath" }
    } else {
        log.trace { "Saving image to file $absolutePath" }
    }
    writeBytes(imageBinaryData ?: notFoundDefaultImage)
}

data class PartnerAndImageUrl(
    val partnerId: Int,
    val imageUrl: String,
)

class PartnerAndImageBytes(
    val partnerId: Int,
    val imageBytes: ByteArray,
) {
    fun inputStream() = imageBytes.inputStream()
    override fun toString() = "PartnerAndImageBytes[partnerId=$partnerId,imageBytes=...]"
}

data class WorkoutAndImageUrl(
    val workoutId: Int,
    val imageUrl: String,
)

class WorkoutAndImagesBytes(
    val workoutId: Int,
    val imageBytes: ByteArray,
) {
    fun inputStream() = imageBytes.inputStream()
    override fun toString() = "WorkoutAndImagesBytes[workoutId=$workoutId,imageBytes...]"
}
