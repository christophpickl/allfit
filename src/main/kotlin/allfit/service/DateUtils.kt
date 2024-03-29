package allfit.service

import allfit.presentation.models.DateRange
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

val zoneAmsterdam: ZoneId = ZoneId.of("Europe/Amsterdam")
private val zoneUtc: ZoneId = ZoneId.of("UTC")

private val isoOffsetFormatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME // 2011-12-03T10:15:30+01:00
private val dayDateAndTimeFormatter = DateTimeFormatter.ofPattern("E dd.MM. HH:mm", Locale.ENGLISH)
private val dayDateYearAndTimeFormatter = DateTimeFormatter.ofPattern("E dd.MM.yy HH:mm", Locale.ENGLISH)
private val dayDateFormatter = DateTimeFormatter.ofPattern("E dd.MM.", Locale.ENGLISH)
private val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.", Locale.ENGLISH)
private val timeOnlyFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale.ENGLISH)
val fileSafeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("YYYY_MM_dd-HH_mm_ss", Locale.ENGLISH)

fun ZonedDateTime.formatIsoOffset(): String = isoOffsetFormatter.format(this)
fun ZonedDateTime.formatDayDate(): String = dayDateFormatter.format(this)
fun ZonedDateTime.formatDate(): String = dateFormatter.format(this)
fun ZonedDateTime.formatTime(): String = timeOnlyFormatter.format(this)
fun ZonedDateTime.formatFileSafe(): String = fileSafeFormatter.format(this)

fun ZonedDateTime.toUtcLocalDateTime(): LocalDateTime = withZoneSameInstant(zoneUtc).toLocalDateTime()

fun LocalDateTime.fromUtcToAmsterdamZonedDateTime(): ZonedDateTime =
    ZonedDateTime.of(this, zoneUtc).withZoneSameInstant(zoneAmsterdam)

fun LocalDate.toZonedDate(): ZonedDateTime = atStartOfDay(zoneAmsterdam)

fun ZonedDateTime.toDaysUntil(numberOfDays: Int): List<ZonedDateTime> =
    (0 until numberOfDays).map {
        this.plusDays(it.toLong())
    }

fun DateRange.formatStartAndEnd(showYear: Boolean) =
    start.format(if (showYear) dayDateYearAndTimeFormatter else dayDateAndTimeFormatter) + "-" + end.format(
        timeOnlyFormatter
    )

fun ZonedDateTime.toPrettyString(clock: Clock): String =
    format(if (year != clock.now().year) dayDateYearAndTimeFormatter else dayDateAndTimeFormatter)

fun ZonedDateTime.beginOfDay(): ZonedDateTime =
    withHour(0)
        .withMinute(0)
        .withSecond(0)
        .withNano(0)

fun ZonedDateTime.endOfDay(): ZonedDateTime =
    withHour(23)
        .withMinute(59)
        .withSecond(59)
        .withNano(0)

fun LocalDateTime.endOfDay(): LocalDateTime =
    withHour(23)
        .withMinute(59)
        .withSecond(59)
        .withNano(0)
