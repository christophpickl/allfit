package allfit.service

import java.time.LocalDateTime
import java.time.ZonedDateTime

interface Clock {
    fun now(): ZonedDateTime

    fun todayBeginOfDay(): ZonedDateTime = now().withHour(0).withMinute(0).withSecond(0)
}

object SystemClock : Clock {
    override fun now(): ZonedDateTime = ZonedDateTime.now(zoneAmsterdam)
}

class DummyDayClock(private val fixedDateTime: LocalDateTime) : Clock {
    override fun now(): ZonedDateTime = fixedDateTime.fromUtcToAmsterdamZonedDateTime()
}
