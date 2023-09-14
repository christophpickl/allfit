package allfit.persistence

import allfit.domain.Location
import allfit.persistence.domain.ExposedWorkoutsRepo
import allfit.persistence.domain.PartnerEntity
import allfit.persistence.domain.WorkoutEntity
import allfit.persistence.testInfra.DbListener
import allfit.persistence.testInfra.ExposedTestRepo
import allfit.persistence.testInfra.workoutEntity
import allfit.service.SystemClock
import allfit.service.toUtcLocalDateTime
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldBeSingleton
import io.kotest.matchers.string.shouldContain
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import org.jetbrains.exposed.exceptions.ExposedSQLException

class ExposedWorkoutsRepoTest : DescribeSpec() {

    private val repo = ExposedWorkoutsRepo
    private val now = SystemClock.now().toUtcLocalDateTime()
    private val workout = Arb.workoutEntity().next().copy(start = now, end = now.plusHours(1))
    private val past = now.minusSeconds(1)
    private val future = now.plusSeconds(1)
    private val someLocation = Location.Haarlem
    private val locationA = Location.Arnhem
    private val locationB = Location.Breda

    init {
        extension(DbListener())

        describe("When select") {
            it("Given workout in the past Then return nothing") {
                insertWorkoutAndCo { it.copy(start = past) }

                val workouts = repo.selectAllStartingFrom(now)

                workouts.shouldBeEmpty()
            }
            it("Given workout in the future Then return something") {
                insertWorkoutAndCo { it.copy(start = future) }

                val workouts = repo.selectAllStartingFrom(now)

                workouts.shouldBeSingleton()
            }
        }
        describe("When select for location") {
            it("Given partner matches location Then return workout") {
                insertWorkoutAndCo(withPartner = { it.copy(locationShortCode = someLocation.shortCode) })

                val workouts = repo.selectAllForLocation(someLocation)

                workouts.shouldBeSingleton()
            }
            it("Given partner mismatches location Then return nothing") {
                insertWorkoutAndCo(withPartner = { it.copy(locationShortCode = locationA.shortCode) })

                val workouts = repo.selectAllForLocation(locationB)

                workouts.shouldBeEmpty()
            }
        }
        describe("When insert") {
            it("Given no partner Then fail") {
                shouldThrow<ExposedSQLException> {
                    repo.insertAll(listOf(workout))
                }.message shouldContain workout.partnerId.toString()
            }
        }
    }

    private fun insertWorkoutAndCo(
        withPartner: (PartnerEntity) -> PartnerEntity = { it },
        withWorkout: (WorkoutEntity) -> WorkoutEntity = { it },
    ) {
        ExposedTestRepo.insertCategoryPartnerAndWorkout(
            withPartner = withPartner,
            withWorkout = { _, _, w ->
                withWorkout(w)
            })
    }

}
