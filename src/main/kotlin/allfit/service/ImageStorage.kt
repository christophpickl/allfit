package allfit.service

import allfit.presentation.PresentationConstants
import allfit.sync.core.SyncListenerManager
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import java.io.File
import javafx.scene.image.Image
import kotlin.collections.set
import kotlinx.coroutines.delay
import mu.KotlinLogging.logger

private fun readClasspathImageBytes(fileName: String): ByteArray =
    ImageStorage::class.java.classLoader
        .getResourceAsStream("images/$fileName")?.readAllBytes()
        ?: error("Classpath image 'images/$fileName' not found!")

private val notFoundDefaultImageBytes = readClasspathImageBytes("not_found_default_image.jpg")
private val prototypeImageBytes = readClasspathImageBytes("prototype.jpg")
private val dropinImageBytes = readClasspathImageBytes("dropin.jpg")

object Images {
    val prototype = Image(prototypeImageBytes.inputStream())
    val notFound = Image(notFoundDefaultImageBytes.inputStream())
    val dropin = Image(dropinImageBytes.inputStream())
}

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
    private val width = PresentationConstants.downloadImageWidth
    private val extension = "jpg"
    private val parallelWorkersCount = 3
    private val delayBetweenEachDownloadInMs = 40L
    private val maxRetryDownloadAttempts = 3

    override suspend fun savePartnerImages(partners: List<PartnerAndImageUrl>) {
        log.debug { "Saving ${partners.size} partner images." }
        partners.mapNotNull { it.imageUrl }.requireAllEndsWithExtension(extension)
        partners.workParallel(parallelWorkersCount, {
            syncListeners.onSyncDetail("Saving ${(it * 100).toInt()}% of partner images done.")
        }) {
            val partnerId = it.partnerId
            val imageUrl = it.imageUrl
            val bytes: ByteArray? = imageUrl?.let { url ->
                val fullUrl = "${url}?w=$width"
                log.debug { "Downloading partner (ID=${partnerId}) image from: $fullUrl" }
                client.retryGetBytes(fullUrl, maxRetryDownloadAttempts)
            } ?: null.also {
                log.debug { "No partner image defined with ID: $partnerId" }
            }
            partnerTarget(partnerId).saveAndLogOrDefault(bytes)
            delay(delayBetweenEachDownloadInMs)
        }
    }


    override fun saveDefaultImageForPartner(partnerIds: List<Int>) {
        partnerIds.forEach { id ->
            partnerTarget(id).saveAndLogOrDefault(null)
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
            val bytes = client.retryGetBytes("${workout.imageUrl}?w=$width", maxRetryDownloadAttempts)
            workoutTarget(workout.workoutId).saveAndLogOrDefault(bytes)
            delay(delayBetweenEachDownloadInMs)
        }
    }

    override fun saveDefaultImageForWorkout(workoutIds: List<Int>) {
        workoutIds.forEach { id ->
            workoutTarget(id).saveAndLogOrDefault(null)
        }
    }

    private fun workoutTarget(workoutId: Int) = File(workoutsFolder, "$workoutId.$extension")

    override fun loadWorkoutImages(workoutIds: List<Int>): List<WorkoutAndImagesBytes> {
        log.debug { "Loading ${workoutIds.size} workout images." }
        return workoutIds.map { id ->
            WorkoutAndImagesBytes(
                workoutId = id,
                imageBytes = loadWorkoutImage(id),
            )
        }
    }

    private fun loadWorkoutImage(workoutId: Int): ByteArray {
        val file = File(workoutsFolder, "$workoutId.$extension")
        return if (file.exists()) {
            file.readBytes()
        } else {
            notFoundDefaultImageBytes
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
            error("Invalid image URL (must end with '.$extension'): $it")
        }
    }
}

private val log = logger {}

private suspend fun HttpClient.retryGetBytes(url: String, maxAttempts: Int, currentAttempt: Int = 1): ByteArray? =
    try {
        log.debug { "Get bytes from [$url] (attempt $currentAttempt/$maxAttempts)" }
        getBytes(url)
    } catch (e: Exception) {
        log.warn(e) { "Failed to download image attempt $currentAttempt/$maxAttempts from: $url" }
        if (currentAttempt == maxAttempts) {
            throw e
        } else {
            retryGetBytes(url, currentAttempt + 1)
        }
    }

private suspend fun HttpClient.getBytes(url: String): ByteArray? {
    val response = get(url)
    if (response.status == HttpStatusCode.NotFound) {
        log.warn { "Received 404 for $url" }
        return null
    }
    response.requireOk()
    return response.body<ByteArray>()
}

private fun File.saveAndLogOrDefault(imageBinaryData: ByteArray?) {
    if (exists()) {
        log.debug { "Overwriting image file $absolutePath" }
    } else {
        log.trace { "Saving image to file $absolutePath" }
    }
    writeBytes(imageBinaryData ?: notFoundDefaultImageBytes)
}

data class PartnerAndImageUrl(
    val partnerId: Int,
    val imageUrl: String?,
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
