package allfit.persistence

import allfit.persistence.domain.ExposedCheckinsRepository
import allfit.persistence.domain.ExposedWorkoutsRepo
import allfit.persistence.domain.PartnerAndCheckins
import allfit.persistence.testInfra.*
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldBeSingleton
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next

class ExposedCheckinsRepositoryTest : StringSpec() {

    private val repo = ExposedCheckinsRepository

    init {
        extension(DbListener())

        "When insert workout-checkin without foreign references Then fail" {
            shouldThrow<Exception> {
                repo.insertAll(listOf(Arb.checkinEntityWorkout().next()))
            }
        }
        "When insert dropin-checkin without foreign references Then fail" {
            shouldThrow<Exception> {
                repo.insertAll(listOf(Arb.checkinEntityDropin().next()))
            }
        }
        "Given workout-checkin and requirements inserted When select all Then return it" {
            val checkin = ExposedTestRepo.insertCategoryPartnerWorkoutAndWorkoutCheckin().fourth

            val checkins = repo.selectAll()

            checkins.shouldBeSingleton().first() shouldBe checkin
        }
        "Given partner with 1 workout-checkin When selectCountForPartners Then return 1" {
            val partner = ExposedTestRepo.insertCategoryPartnerWorkoutAndWorkoutCheckin().second

            val checkins = repo.selectCountForPartners()

            checkins.shouldBeSingleton().first() shouldBe PartnerAndCheckins(
                partnerId = partner.id,
                checkinsCount = 1,
            )
        }
        "Given partner with 1 dropin-checkin When selectCountForPartners Then return 1" {
            val partner = ExposedTestRepo.insertCategoryPartnerAndDropinCheckin().second

            val checkins = repo.selectCountForPartners()

            checkins.shouldBeSingleton().first() shouldBe PartnerAndCheckins(
                partnerId = partner.id,
                checkinsCount = 1,
            )
        }
        "Given partner with 2 workout-checkins When selectCountForPartners Then return 2" {
            val partner = ExposedTestRepo.insertCategoryPartnerWorkoutAndWorkoutCheckin().second
            val workout2 = Arb.workoutEntity().next().copy(partnerId = partner.id)
            ExposedWorkoutsRepo.insertAll(listOf(workout2))
            ExposedCheckinsRepository.insertAll(listOf(Arb.checkinEntityWorkout().next().copy(workoutId = workout2.id)))

            val checkins = repo.selectCountForPartners()

            checkins.shouldBeSingleton().first() shouldBe PartnerAndCheckins(
                partnerId = partner.id,
                checkinsCount = 2,
            )
        }
        "Given partner without checkin When selectCountForPartners Then return 0" {
            val partner = ExposedTestRepo.insertCategoryPartnerAndWorkout().second

            val checkins = repo.selectCountForPartners()

            checkins.shouldBeSingleton().first() shouldBe PartnerAndCheckins(
                partnerId = partner.id,
                checkinsCount = 0,
            )
        }
    }
}
