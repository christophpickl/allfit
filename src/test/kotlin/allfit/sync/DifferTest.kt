package allfit.sync

import allfit.api.models.CategoryJsonDefinition
import allfit.api.models.categoryJson
import allfit.domain.category
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldBeSingleton
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next

class DifferTest : StringSpec() {

    private val id = 42
    private val mapper = CategoryJsonDefinition::toCategory

    init {
        "Given remote When diff Then insert it" {
            val report = diffLocalToRemote(emptyList(), listOf(id))

            report.toInsert.shouldBeSingleton().first().id shouldBe id
            report.toDelete.shouldBeEmpty()
        }
        "Given same local and remote When diff Then skip it" {
            val report = diffLocalToRemote(listOf(id), listOf(id))

            report.toInsert.shouldBeEmpty()
            report.toDelete.shouldBeEmpty()
        }
        "Given local When sync Then delete it" {
            val report = diffLocalToRemote(listOf(id), emptyList())

            report.toInsert.shouldBeEmpty()
            report.toDelete.shouldBeSingleton().first().id shouldBe id
        }
    }

    private fun diffLocalToRemote(localDboIds: List<Int>, remoteJsonIds: List<Int>) =
        Differ.diff(localDbosWithId(localDboIds), remoteJsonsWithId(remoteJsonIds), mapper)

    private fun localDbosWithId(ids: List<Int>) = ids.map { id ->
        Arb.category().next().copy(id = id)
    }

    private fun remoteJsonsWithId(ids: List<Int>) = ids.map { id ->
        Arb.categoryJson().next().copy(id = id)
    }
}
