package allfit.service

import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.string
import java.time.ZonedDateTime

fun Arb.Companion.insertWorkout() = arbitrary {
    InsertWorkout(
        id = int(min = 1).next(),
        partnerId = int(min = 1).next(),
        slug = string().next(),
        name = string().next(),
        from = ZonedDateTime.now(),
        till = ZonedDateTime.now(),
    )
}
