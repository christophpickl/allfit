package allfit.persistence

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldBeSingleton
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next

class ExposedLocationsRepoTest : DescribeSpec() {

    private val repo = ExposedLocationsRepo
    private val partner = Arb.partnerEntity().next().copy(
        categoryIds = emptyList()
    )
    private val locationWithPartner = Arb.locationEntity().next().copy(
        partnerId = partner.id
    )

    init {
        extension(DbListener())

        describe("When select") {
            it("Then succeed") {
                repo.selectAll()
            }
        }
        describe("When insert") {
            it("Then succeed") {
                ExposedPartnersRepo.insertAll(listOf(partner))
                repo.insertAllIfNotYetExists(listOf(locationWithPartner))

                repo.selectAll().shouldBeSingleton().first() shouldBe locationWithPartner
            }
            it("Given same Then ignore") {
                ExposedPartnersRepo.insertAll(listOf(partner))
                repo.insertAllIfNotYetExists(listOf(locationWithPartner))

                repo.insertAllIfNotYetExists(listOf(locationWithPartner))
                repo.selectAll().shouldBeSingleton().first() shouldBe locationWithPartner
            }
        }
    }
}