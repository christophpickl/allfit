package allfit.service

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime

class DateKtTest : DescribeSpec() {

    private val zoneAmsterdam: ZoneId = ZoneId.of("Europe/Amsterdam")
    private val zoneUtc: ZoneId = ZoneId.of("UTC")
    private val zone: ZoneId = zoneAmsterdam

    init {
        describe("Date transformers") {
            it("ZonedDateTime.toUtcDate") {
                val zonedDate = ZonedDateTime.of(2000, 1, 1, 15, 0, 0, 0, zone)
                zonedDate.toUtcLocalDateTime() shouldBe LocalDateTime.of(2000, 1, 1, 15, 0, 0, 0)
                    .minusSeconds(zonedDate.offset.totalSeconds.toLong())
            }
            it("LocalDateTime.toZonedDateTime") {
                val zonedDate = ZonedDateTime.of(2000, 1, 1, 15, 0, 0, 0, zone)
                val localDate = LocalDateTime.of(2000, 1, 1, 15, 0, 0, 0)
                    .minusSeconds(zonedDate.offset.totalSeconds.toLong())
                localDate.fromUtcToAmsterdamZonedDateTime() shouldBe zonedDate
            }
        }
        describe("Formatter") {
            it("ZonedDateTime.formatOnefit") {
                ZonedDateTime.of(2000, 1, 2, 10, 11, 12, 0, zoneUtc).formatIsoOffset() shouldBe "2000-01-02T10:11:12Z"
            }
        }
        describe("SystemClock") {
            it("now") {
                SystemClock.now().zone shouldBe zoneAmsterdam
            }
        }
    }
}
