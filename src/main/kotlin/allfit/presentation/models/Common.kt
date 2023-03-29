package allfit.presentation.models

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

typealias Rating = Int


data class DateRange(
    val start: ZonedDateTime,
    val end: ZonedDateTime,
) : Comparable<DateRange> {
    companion object {
        private val formatterDateTime = DateTimeFormatter.ofPattern("E dd.MM. HH:mm", Locale.ENGLISH)
        private val formatterTime = DateTimeFormatter.ofPattern("HH:mm", Locale.ENGLISH)
        private fun format(date: DateRange) =
            date.start.format(formatterDateTime) + "-" + date.end.format(formatterTime)
    }

    val prettyString = format(this)
    val durationInMinutes = ChronoUnit.MINUTES.between(start, end)

    init {
        require(start.isBefore(end)) { "Start ($start) must be before end ($end)." }
    }

    override fun compareTo(other: DateRange): Int {
        val startDiff = start.compareTo(other.start)
        if (startDiff != 0) return startDiff
        return end.compareTo(other.end)
    }
}
