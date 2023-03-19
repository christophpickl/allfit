package allfit.service

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.delay
import mu.KotlinLogging.logger
import java.io.File

interface ImageStorage {

    suspend fun savePartnerImages(partners: List<PartnerAndImageUrl>)
    fun loadPartnerImages(partnerIds: List<Int>): List<PartnerAndImageBytes>

    suspend fun saveWorkoutImages(workouts: List<WorkoutAndImageUrl>)
    fun loadWorkoutImages(workoutIds: List<Int>): List<WorkoutAndImagesBytes>
    fun deleteWorkoutImages(workoutIds: List<Int>)
}

class InMemoryImageStorage : ImageStorage {
    val savedPartnerImages = mutableListOf<PartnerAndImageUrl>()
    val partnerImagesToBeLoaded = mutableMapOf<Int, PartnerAndImageBytes>()
    val savedWorkoutImages = mutableListOf<WorkoutAndImageUrl>()
    val workoutImagesToBeLoaded = mutableMapOf<Int, WorkoutAndImagesBytes>()

    override suspend fun savePartnerImages(partners: List<PartnerAndImageUrl>) {
        savedPartnerImages += partners
    }

    override fun loadPartnerImages(partnerIds: List<Int>): List<PartnerAndImageBytes> =
        partnerIds.mapNotNull {
            partnerImagesToBeLoaded[it]
        }

    override suspend fun saveWorkoutImages(workouts: List<WorkoutAndImageUrl>) {
        savedWorkoutImages += workouts
    }

    override fun loadWorkoutImages(workoutIds: List<Int>): List<WorkoutAndImagesBytes> =
        workoutIds.mapNotNull {
            workoutImagesToBeLoaded[it]
        }

    override fun deleteWorkoutImages(workoutIds: List<Int>) {
    }
}

class RealImageStorage(
    private val partnersFolder: File,
    private val workoutsFolder: File,
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
            client.getAndSave("${it.imageUrl}?w=$width", File(partnersFolder, "${it.partnerId}.$extension"))
            delay(delayBetweenEachDownloadInMs)
        }
    }

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

        workouts.workParallel(parallelWorkersCount) { workout ->
            client.getAndSave(
                url = "${workout.imageUrl}?w=$width",
                target = File(workoutsFolder, "${workout.workoutId}.$extension")
            )
            delay(delayBetweenEachDownloadInMs)
        }
    }

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
        // FIXME implement me
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

private val defaultImage: ByteArray = ImageStorage::class.java.classLoader.getResourceAsStream("not_found_default_image.jpg")!!
    .readAllBytes()

private suspend fun HttpClient.getAndSave(url: String, target: File) {
    val response = get(url)
    if (response.status == HttpStatusCode.NotFound) {
        target.writeBytes(defaultImage)
        log.warn { "Received 404 for $url - saving default not found image instead to: ${target.absolutePath}" }
        return
    }
    response.requireOk()
    val imageBinaryData = response.body<ByteArray>()
    if (target.exists()) {
        log.warn { "Overwriting image file ${target.absolutePath} from URL: $url" }
    } else {
        log.trace { "Saving image to file ${target.absolutePath} from URL: $url" }
    }
    target.writeBytes(imageBinaryData)
}

data class PartnerAndImageUrl(
    val partnerId: Int,
    val imageUrl: String,
)

class PartnerAndImageBytes(
    val partnerId: Int,
    val imageBytes: ByteArray,
) {
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
    override fun toString() = "WorkoutAndImagesBytes[workoutId=$workoutId,imageBytes...]"
}
