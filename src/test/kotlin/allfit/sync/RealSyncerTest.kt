package allfit.sync

import allfit.api.InMemoryOnefitClient
import allfit.persistence.InMemoryLocationsRepo
import allfit.persistence.InMemoryPartnersRepo
import allfit.persistence.InMemoryReservationsRepo
import allfit.persistence.InMemoryWorkoutsRepo
import allfit.service.InMemoryImageStorage
import io.kotest.core.spec.style.StringSpec
import io.kotest.core.test.TestCase

class RealSyncerTest : StringSpec() {

    private lateinit var client: InMemoryOnefitClient
    private lateinit var partnersRepo: InMemoryPartnersRepo
    private lateinit var locationsRepo: InMemoryLocationsRepo
    private lateinit var workoutsRepo: InMemoryWorkoutsRepo
    private lateinit var workoutFetcher: WorkoutFetcher
    private lateinit var reservationsRepo: InMemoryReservationsRepo
    private lateinit var imageStorage: InMemoryImageStorage


    override suspend fun beforeEach(testCase: TestCase) {
        client = InMemoryOnefitClient()
        locationsRepo = InMemoryLocationsRepo()
        partnersRepo = InMemoryPartnersRepo()
        workoutsRepo = InMemoryWorkoutsRepo()
        workoutFetcher = DummyWorkoutFetcher()
        reservationsRepo = InMemoryReservationsRepo()
        imageStorage = InMemoryImageStorage()
    }

    init {
        // FIXME test transactionality; let it insert in DB; then fail; then it should be backrolled
    }

}

