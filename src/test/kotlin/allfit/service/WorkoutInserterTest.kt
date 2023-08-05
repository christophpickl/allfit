package allfit.service

import allfit.persistence.domain.InMemoryWorkoutsRepo
import allfit.sync.domain.InMemoryWorkoutFetcher
import allfit.sync.domain.WorkoutUrl
import allfit.sync.domain.workoutFetch
import io.kotest.core.spec.style.StringSpec
import io.kotest.core.test.TestCase
import io.kotest.matchers.collections.shouldBeSingleton
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next

class WorkoutInserterTest : StringSpec() {

    private val insertData = Arb.insertWorkout().next()
    private val workoutFetch = Arb.workoutFetch().next()
    private lateinit var listener: InMemoryWorkoutInsertListener
    private lateinit var workoutsRepo: InMemoryWorkoutsRepo
    private lateinit var workoutFetcher: InMemoryWorkoutFetcher
    private lateinit var imageStorage: InMemoryImageStorage
    private lateinit var inserter: WorkoutInserter

    override suspend fun beforeEach(testCase: TestCase) {
        listener = InMemoryWorkoutInsertListener()
        workoutsRepo = InMemoryWorkoutsRepo()
        workoutFetcher = InMemoryWorkoutFetcher()
        imageStorage = InMemoryImageStorage()
        inserter = WorkoutInserterImpl(
            workoutsRepo = workoutsRepo,
            workoutFetcher = workoutFetcher,
            imageStorage = imageStorage,
        )
    }

    init {
        "Given workout fetch result When insert Then insert workout and save image" {
            val workoutUrl = insertData.toWorkoutUrl()
            workoutFetcher.urlToFetches[workoutUrl] = workoutFetch.copy(
                workoutId = insertData.id,
                imageUrls = listOf("imageUrl")
            )

            inserter.insert(listOf(insertData), listener)

            workoutsRepo.workouts.values.shouldBeSingleton().first().also { entity ->
                entity.id shouldBe insertData.id
            }
            imageStorage.savedWorkoutImages.shouldBeSingleton().first().also { given ->
                given.workoutId shouldBe workoutUrl.workoutId
                given.imageUrl shouldBe "imageUrl"
            }
        }
    }
}

fun InsertWorkout.toWorkoutUrl() = WorkoutUrl(
    workoutId = this.id,
    workoutSlug = this.slug,
)

class InMemoryWorkoutInsertListener : WorkoutInsertListener {
    val messages = mutableListOf<String>()
    override fun onProgress(message: String) {
        messages += message
    }

}