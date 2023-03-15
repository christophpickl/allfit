package allfit.service

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
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

    override suspend fun savePartnerImages(partners: List<PartnerAndImageUrl>) {
        log.debug { "Saving ${partners.size} partner images." }
        partners.map { it.imageUrl }.requireAllEndsWithExtension(extension)
        partners.forEach {
            client.getAndSave("${it.imageUrl}?w=$width", File(partnersFolder, "${it.partnerId}.$extension"))
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
        workouts.map { it.imagesUrl }.flatten().requireAllEndsWithExtension(extension)
        workouts.forEach { workout ->
            workout.imagesUrl.forEachIndexed { i, imageUrl ->
                client.getAndSave("$imageUrl?w=$width", File(workoutsFolder, "${workout.workoutId}-$i.$extension"))
            }
        }
    }

    override fun loadWorkoutImages(workoutIds: List<Int>): List<WorkoutAndImagesBytes> {
        log.debug { "Loading ${workoutIds.size} workout images." }
        return workoutIds.map { id ->
            WorkoutAndImagesBytes(
                workoutId = id,
                imagesBytes = loadImageBytesFor(id),
            )
        }
    }

    private fun loadImageBytesFor(workoutId: Int): List<ByteArray> {
        val prefix = "$workoutId-"
        return workoutsFolder.list { _, name ->
            name.startsWith(prefix)
        }!!.map {
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

private suspend fun HttpClient.getAndSave(url: String, target: File) {
    val response = get(url)
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
    val imagesUrl: List<String>,
)

class WorkoutAndImagesBytes(
    val workoutId: Int,
    val imagesBytes: List<ByteArray>,
) {
    override fun toString() = "WorkoutAndImagesBytes[workoutId=$workoutId,imagesBytes.size=${imagesBytes.size}]"
}
