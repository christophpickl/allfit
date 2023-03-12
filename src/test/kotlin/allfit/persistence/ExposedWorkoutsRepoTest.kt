package allfit.persistence

import allfit.domain.PartnerNotFoundException
import allfit.domain.Workout
import allfit.domain.category
import allfit.domain.partner
import allfit.domain.workout
import allfit.service.SystemClock
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldBeSingleton
import io.kotest.matchers.string.shouldContain
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next

class ExposedWorkoutsRepoTest : DescribeSpec() {

    private val repo = ExposedWorkoutsRepo
    private val category = Arb.category().next()
    private val partnerWithCategory = Arb.partner().next().copy(categories = listOf(category))
    private val now = SystemClock.now()
    private val workout = Arb.workout().next().copy(start = now, end = now.plusHours(1))
    private val past = now.minusSeconds(1)
    private val future = now.plusSeconds(1)

    init {
        extension(DbListener())

        describe("When select") {
            it("Given workout in the past Then return nothing") {
                repo.insert(listOf(insertWorkoutRequirements { it.copy(start = past) }))

                val workouts = repo.selectStartingFrom(now)

                workouts.shouldBeEmpty()
            }
            it("Given workout in the future Then return nothing") {
                repo.insert(listOf(insertWorkoutRequirements { it.copy(start = future) }))

                val workouts = repo.selectStartingFrom(now)

                workouts.shouldBeSingleton()
            }
        }
        describe("When insert") {
            it("Given no partner Then fail") {
                shouldThrow<PartnerNotFoundException> {
                    repo.insert(listOf(workout))
                }.message shouldContain workout.partner.id.toString()
            }
        }
    }

    private fun insertWorkoutRequirements(code: (Workout) -> Workout = { it }): Workout {
        ExposedCategoriesRepo.insert(listOf(category))
        ExposedPartnersRepo.insert(listOf(partnerWithCategory))
        return workout.let(code).copy(partner = partnerWithCategory)
    }
}
