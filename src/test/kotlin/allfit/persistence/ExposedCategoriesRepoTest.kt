package allfit.persistence

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next

class ExposedCategoriesRepoTest : DescribeSpec() {
    init {
        include(repoTests(RepoTestContext(ExposedCategoriesRepo) {
            var dbo = Arb.categoryDbo().next()
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
