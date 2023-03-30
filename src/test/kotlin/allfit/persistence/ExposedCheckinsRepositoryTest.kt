package allfit.persistence

import allfit.persistence.domain.CheckinEntity
import allfit.persistence.domain.ExposedCheckinsRepository
import allfit.persistence.domain.ExposedWorkoutsRepo
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldBeSingleton
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next

class ExposedCheckinsRepositoryTest : StringSpec() {

    private val repo = ExposedCheckinsRepository
    private val anyCheckin = Arb.checkinEntity().next()

    init {
        extension(DbListener())

        "When insert checkin without foreign references Then fail" {
            shouldThrow<Exception> {
                repo.insertAll(listOf(anyCheckin))
            }
        }
        "Given checkin and requirements inserted When select all Then return it" {
            val checkin = insertCheckinForeignReferences()
            repo.insertAll(listOf(checkin))

            val checkins = repo.selectAll()

            checkins.shouldBeSingleton().first() shouldBe checkin
        }
    }

    private fun insertCheckinForeignReferences(): CheckinEntity {
        val partner = Arb.partnerEntity().next().copy(categoryIds = emptyList())
        ExposedPartnersRepo.insertAll(listOf(partner))
        val workout = Arb.workoutEntity().next().copy(partnerId = partner.id)
        ExposedWorkoutsRepo.insertAll(listOf(workout))
        return Arb.checkinEntity().next().copy(workoutId = workout.id)
    }
}
