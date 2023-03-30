package allfit.persistence

import allfit.persistence.domain.ExposedCategoriesRepo
import allfit.persistence.domain.ExposedPartnersRepo
import allfit.persistence.domain.ExposedWorkoutsRepo
import allfit.persistence.domain.WorkoutEntity
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
    private val category = Arb.categoryEntity().next()
    private val partnerWithCategory = Arb.partnerEntity().next().copy(categoryIds = listOf(category.id))
    private val now = SystemClock.now().toUtcLocalDateTime()
    private val workout = Arb.workoutEntity().next().copy(start = now, end = now.plusHours(1))
    private val past = now.minusSeconds(1)
    private val future = now.plusSeconds(1)

    init {
        extension(DbListener())

        describe("When select") {
            it("Given workout in the past Then return nothing") {
                repo.insertAll(listOf(insertWorkoutRequirements { it.copy(start = past) }))

                val workouts = repo.selectAllStartingFrom(now)

                workouts.shouldBeEmpty()
            }
            it("Given workout in the future Then return nothing") {
                repo.insertAll(listOf(insertWorkoutRequirements { it.copy(start = future) }))

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

    private fun insertWorkoutRequirements(code: (WorkoutEntity) -> WorkoutEntity = { it }): WorkoutEntity {
        ExposedCategoriesRepo.insertAll(listOf(category))
        ExposedPartnersRepo.insertAll(listOf(partnerWithCategory))
        return workout.let(code).copy(partnerId = partnerWithCategory.id)
    }
}
