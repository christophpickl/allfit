package allfit.presentation.models

import allfit.service.Clock
import allfit.service.formatStartAndEnd
import allfit.service.zoneAmsterdam
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

typealias Rating = Int

data class DateRange(
    val start: ZonedDateTime,
    val end: ZonedDateTime,
) : Comparable<DateRange> {

    companion object {
        private val zeroDate = ZonedDateTime.of(2000, 1, 1, 0, 0, 0, 0, zoneAmsterdam)
        val NONE = DateRange(zeroDate, zeroDate)
    }

    fun toPrettyString(clock: Clock) =
        formatStartAndEnd(showYear = start.year != clock.now().year)

    val durationInMinutes = ChronoUnit.MINUTES.between(start, end)

    init {
        require(start.isEqual(end) || start.isBefore(end)) { "START $start must be before-equal END $end." }
    }

    operator fun contains(value: ZonedDateTime): Boolean =
        value in start..end

    override fun compareTo(other: DateRange): Int {
        val startDiff = start.compareTo(other.start)
        if (startDiff != 0) return startDiff
        return end.compareTo(other.end)
    }
}

enum class Trilean {
    Yes, No, Unknown;
}

fun Boolean?.toTrilean() = when (this) {
    true -> Trilean.Yes
    false -> Trilean.No
    null -> Trilean.Unknown
}
