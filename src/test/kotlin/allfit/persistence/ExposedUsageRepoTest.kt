package allfit.persistence

import allfit.persistence.domain.ExposedUsageRepo
import allfit.persistence.testInfra.DbListener
import allfit.persistence.testInfra.usageEntity
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import org.junit.jupiter.api.assertThrows

class ExposedUsageRepoTest : DescribeSpec() {

    private val repo = ExposedUsageRepo
    private val usage = Arb.usageEntity().next()
    private val usage1 = usage.copy(total = 1)
    private val usage2 = usage.copy(total = 2)

    init {
        extension(DbListener())

        describe("When select one") {
            it("Then throw") {
                assertThrows<Exception> {
                    repo.selectOne()
                }
            }
            it("Given one Then return it") {
                repo.upsert(usage)

                repo.selectOne() shouldBe usage
            }
        }

        describe("When upsert") {
            it("Given one Then update it") {
                repo.upsert(usage1)

                repo.upsert(usage2)

                repo.selectOne() shouldBe usage2
            }
        }
    }
}