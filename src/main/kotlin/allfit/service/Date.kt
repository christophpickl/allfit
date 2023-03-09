package allfit.service

import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

// 2011-12-03T10:15:30+01:00
private val formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME
val zone: ZoneId = ZoneId.of("Europe/Amsterdam")

fun ZonedDateTime.formatOnefit(): String = formatter.format(this)
