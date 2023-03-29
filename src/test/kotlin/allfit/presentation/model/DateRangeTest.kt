package allfit.presentation.model

import allfit.presentation.models.DateRange
import allfit.service.fromUtcToAmsterdamZonedDateTime
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import java.time.LocalDateTime

class DateRangeTest : StringSpec() {
    private val now = LocalDateTime.parse("2000-01-02T11:13:14").fromUtcToAmsterdamZonedDateTime()

    init {
        "pretty string" {
            DateRange(now, now.plusHours(1)).prettyString shouldBe "Sun 02.01. 12:13-13:13"
        }
        "duration in minutes" {
            DateRange(now, now.plusMinutes(42)).durationInMinutes shouldBe 42
        }
        "sortable via comparable" {
            val date1 = DateRange(now, now.plusHours(1))
            val date2 = DateRange(now, now.plusHours(2))
            val date3 = DateRange(now.plusMinutes(1), now.plusMinutes(2))
            val unsorted = listOf(date3, date1, date2)

            unsorted.sorted() shouldContainExactly listOf(date1, date2, date3)
        }
    }
}
