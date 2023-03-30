package allfit.persistence

import allfit.persistence.domain.ExposedCategoriesRepo
import allfit.persistence.domain.ExposedLocationsRepo
import allfit.persistence.domain.ExposedPartnersRepo
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldBeSingleton
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next

class ExposedLocationsRepoTest : DescribeSpec() {

    private val repo = ExposedLocationsRepo
    private val category = Arb.categoryEntity().next()
    private val partner = Arb.partnerEntity().next().copy(
        primaryCategoryId = category.id,
        secondaryCategoryIds = emptyList(),
    )
    private val location = Arb.locationEntity().next().copy(
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
                setupRequirements()
                repo.insertAllIfNotYetExists(listOf(location))

                repo.selectAll().shouldBeSingleton().first() shouldBe location
            }
            it("Given same Then ignore") {
                setupRequirements()
                repo.insertAllIfNotYetExists(listOf(location))

                repo.insertAllIfNotYetExists(listOf(location))
                repo.selectAll().shouldBeSingleton().first() shouldBe location
            }
        }
    }

    private fun setupRequirements() {
        ExposedCategoriesRepo.insertAll(listOf(category))
        ExposedPartnersRepo.insertAll(listOf(partner))
    }
}