package allfit.service

import allfit.presentation.models.DateRange
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

private val isoOffsetFormatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME // 2011-12-03T10:15:30+01:00
private val dayDateAndTimeFormatter = DateTimeFormatter.ofPattern("E dd.MM. HH:mm", Locale.ENGLISH)
private val dayDateFormatter = DateTimeFormatter.ofPattern("E dd.MM.", Locale.ENGLISH)
private val timeOnlyFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale.ENGLISH)

private val zoneAmsterdam: ZoneId = ZoneId.of("Europe/Amsterdam")
private val zoneUtc: ZoneId = ZoneId.of("UTC")

fun ZonedDateTime.formatIsoOffset(): String = isoOffsetFormatter.format(this)
fun ZonedDateTime.formatDayDate(): String = dayDateFormatter.format(this)
fun ZonedDateTime.formatTime(): String = timeOnlyFormatter.format(this)

object SystemClock {
    fun now(): ZonedDateTime = ZonedDateTime.now(zoneAmsterdam)
    fun todayBeginOfDay(): ZonedDateTime = now().withHour(0).withMinute(0).withSecond(0)
}

fun ZonedDateTime.toUtcLocalDateTime(): LocalDateTime =
    withZoneSameInstant(zoneUtc).toLocalDateTime()

fun LocalDateTime.fromUtcToAmsterdamZonedDateTime(): ZonedDateTime =
    ZonedDateTime.of(this, zoneUtc).withZoneSameInstant(zoneAmsterdam)

fun LocalDate.toZonedDate(): ZonedDateTime =
    atStartOfDay(zoneAmsterdam)

fun ZonedDateTime.toDaysUntil(numberOfDays: Int): List<ZonedDateTime> =
    (0 until numberOfDays).map {
        this.plusDays(it.toLong())
    }

fun DateRange.formatStartAndEnd() =
    start.format(dayDateAndTimeFormatter) + "-" + end.format(timeOnlyFormatter)
