package allfit.presentation.model

import allfit.presentation.models.DateRange
import allfit.service.DummyDayClock
import allfit.service.fromUtcToAmsterdamZonedDateTime
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import java.time.LocalDateTime

class DateRangeTest : StringSpec() {

    private val dateTime = LocalDateTime.parse("2005-01-02T11:13:14").fromUtcToAmsterdamZonedDateTime()

    init {
        "pretty string this year" {
            DateRange(dateTime, dateTime.plusHours(1))
                .toPrettyString(clockWithYear(dateTime.year)) shouldBe "Sun 02.01. 12:13-13:13"
        }
        "pretty string this previous year" {
            DateRange(dateTime, dateTime.plusHours(1))
                .toPrettyString(clockWithYear(dateTime.year + 1)) shouldBe "Sun 02.01.05 12:13-13:13"
        }
        "duration in minutes" {
            DateRange(dateTime, dateTime.plusMinutes(42)).durationInMinutes shouldBe 42
        }
        "sortable via comparable" {
            val date1 = DateRange(dateTime, dateTime.plusHours(1))
            val date2 = DateRange(dateTime, dateTime.plusHours(2))
            val date3 = DateRange(dateTime.plusMinutes(1), dateTime.plusMinutes(2))
            val unsorted = listOf(date3, date1, date2)

            unsorted.sorted() shouldContainExactly listOf(date1, date2, date3)
        }
    }

    private fun clockWithYear(year: Int) =
        DummyDayClock(LocalDateTime.now().withYear(year))
}
