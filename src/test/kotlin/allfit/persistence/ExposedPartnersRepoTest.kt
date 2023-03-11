package allfit.persistence

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next

class ExposedPartnersRepoTest : DescribeSpec() {
    init {
        include(repoTests(RepoTestContext(ExposedPartnersRepo) {
            var dbo = Arb.partnerDbo().next()
            if (it.id != null) {
                dbo = dbo.copy(id = it.id)
            }
            if (it.isDeleted != null) {
                dbo = dbo.copy(isDeleted = it.isDeleted)
            }
            dbo
        }))

        describe("FK category constraint") {
            it("success") {
                val category = Arb.categoryDbo().next()
                ExposedCategoriesRepo.insert(listOf(category))

                val partner = Arb.partnerDbo().next().copy(categories = listOf(category.id))
                ExposedPartnersRepo.insert(listOf(partner))
            }
            it("fail") {
                shouldThrow<Exception> {
                    ExposedPartnersRepo.insert(listOf(Arb.partnerDbo().next()))
                }
            }
        }
    }
}
