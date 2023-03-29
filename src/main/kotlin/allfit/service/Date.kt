package allfit.service

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

// 2011-12-03T10:15:30+01:00
private val formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME
private val zoneAmsterdam: ZoneId = ZoneId.of("Europe/Amsterdam")
private val zoneUtc: ZoneId = ZoneId.of("UTC")

fun ZonedDateTime.formatOnefit(): String = formatter.format(this)

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

