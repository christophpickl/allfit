package allfit.persistence

import allfit.persistence.domain.ExposedWorkoutsRepo
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

    init {
        extension(DbListener())

        describe("When select") {
            it("Given workout in the past Then return nothing") {
                repo.insertAll(listOf(ExposedTestRepo.insertCategoryAndPartnerForWorkout { it.copy(start = past) }))

                val workouts = repo.selectAllStartingFrom(now)

                workouts.shouldBeEmpty()
            }
            it("Given workout in the future Then return nothing") {
                repo.insertAll(listOf(ExposedTestRepo.insertCategoryAndPartnerForWorkout { it.copy(start = future) }))

                val workouts = repo.selectAllStartingFrom(now)

                workouts.shouldBeSingleton()
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

}
