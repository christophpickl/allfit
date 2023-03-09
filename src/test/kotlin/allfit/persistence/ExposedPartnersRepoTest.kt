package allfit.persistence

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
    }
}
