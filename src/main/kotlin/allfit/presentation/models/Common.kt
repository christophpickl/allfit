package allfit.presentation.models

import allfit.service.formatStartAndEnd
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

typealias Rating = Int

data class DateRange(
    val start: ZonedDateTime,
    val end: ZonedDateTime,
) : Comparable<DateRange> {

    val prettyString = formatStartAndEnd()
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
