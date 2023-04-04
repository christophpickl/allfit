package allfit.persistence

import allfit.persistence.domain.ExposedLocationsRepo
import allfit.persistence.testInfra.DbListener
import allfit.persistence.testInfra.ExposedTestRepo
import allfit.persistence.testInfra.locationEntity
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldBeSingleton
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next

class ExposedLocationsRepoTest : DescribeSpec() {

    private val repo = ExposedLocationsRepo

    init {
        extension(DbListener())

        describe("When select") {
            it("Then succeed") {
                repo.selectAll()
            }
        }
        describe("When insert") {
            it("Then succeed") {
                val (_, partner) = ExposedTestRepo.insertCategoryAndPartner()
                val location = Arb.locationEntity().next().copy(partnerId = partner.id)

                repo.insertAllIfNotYetExists(listOf(location))

                repo.selectAll().shouldBeSingleton().first() shouldBe location
            }
            it("Given same Then ignore") {
                val (_, partner) = ExposedTestRepo.insertCategoryAndPartner()
                val location = Arb.locationEntity().next().copy(partnerId = partner.id)
                repo.insertAllIfNotYetExists(listOf(location))

                repo.insertAllIfNotYetExists(listOf(location))
                repo.selectAll().shouldBeSingleton().first() shouldBe location
            }
        }
    }
}