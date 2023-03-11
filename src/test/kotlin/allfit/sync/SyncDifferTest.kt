package allfit.sync

import allfit.api.models.CategoryJsonDefinition
import allfit.api.models.categoryJson
import allfit.domain.category
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldBeSingleton
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next

class SyncDifferTest : DescribeSpec() {

    private val jsonId = 42
    private val mapper = CategoryJsonDefinition::toCategory

    init {
        describe("Insert categories") {
            it("Insert single") {
                val report = SyncDiffer.diff(localDbosWithId(), remoteJsonsWithId(jsonId), mapper)

                report.toInsert.shouldBeSingleton().first().id shouldBe jsonId
            }
            it("Skip existing") {
                val report = SyncDiffer.diff(localDbosWithId(jsonId), remoteJsonsWithId(jsonId), mapper)

                report.toInsert.shouldBeEmpty()
            }
        }
        describe("Delete categories") {
            it("Delete single") {
                val report = SyncDiffer.diff(localDbosWithId(jsonId), remoteJsonsWithId(), mapper)

                report.toDelete.shouldBeSingleton().first().id shouldBe jsonId
            }
            it("Skip existing") {
                val report = SyncDiffer.diff(localDbosWithId(jsonId), remoteJsonsWithId(jsonId), mapper)

                report.toDelete.shouldBeEmpty()
            }
        }
    }

    private fun localDbosWithId(vararg ids: Int) = ids.map { id ->
        Arb.category().next().copy(id = id)
    }

    private fun remoteJsonsWithId(vararg ids: Int) = ids.map { id ->
        Arb.categoryJson().next().copy(id = id)
    }
}
