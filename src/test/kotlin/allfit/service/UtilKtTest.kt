package allfit.service

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.delay

class UtilKtTest : DescribeSpec() {
    init {
        describe("workParallel") {
            it("process all items") {
                val items = listOf(1, 2, 3)

                val result = mutableListOf<Int>()
                items.workParallel(1) {
                    result += it
                }

                result shouldContainExactly items
            }
            it("notifies") {
                val percentagesDone = mutableListOf<Double>()
                List(30) { it }.workParallel(1, { percentagesDone += it }, percentageBroadcastIntervalInMs = 50) {
                    delay(40)
                }

                percentagesDone.size shouldBeGreaterThan 2
                percentagesDone.first() shouldBe 0.0
                percentagesDone.last() shouldBe 1.0
                percentagesDone.sorted() shouldBe percentagesDone
            }
        }
    }
}
