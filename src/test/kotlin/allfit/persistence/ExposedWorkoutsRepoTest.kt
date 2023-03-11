package allfit.persistence

import allfit.domain.Workout
import allfit.domain.category
import allfit.domain.partner
import allfit.domain.workout
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldBeSingleton
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next

class ExposedWorkoutsRepoTest : DescribeSpec() {

    private val repo = ExposedWorkoutsRepo
    private val category = Arb.category().next()
    private val partnerWithCategory = Arb.partner().next().copy(categories = listOf(category))
    private val workout = Arb.workout().next()

    init {
        extension(DbListener())

        describe("When select") {
            it("Then succeed") {
                repo.select()
            }
        }
        describe("When insert") {
            it("Given requirements Then returned") {
                val workoutToBeInserted = insertWorkoutRequirements()

                repo.insert(listOf(workoutToBeInserted))

                repo.select().shouldBeSingleton().first() shouldBe workoutToBeInserted
            }
            it("Given no partner Then fail") {
                shouldThrow<PartnerNotFoundException> {
                    repo.insert(listOf(workout))
                }.message shouldContain workout.partner.id.toString()
            }
        }
    }

    private fun insertWorkoutRequirements(): Workout {
        ExposedCategoriesRepo.insert(listOf(category))
        ExposedPartnersRepo.insert(listOf(partnerWithCategory))
        return workout.copy(partner = partnerWithCategory)
    }

}