package allfit.persistence

import io.kotest.property.Arb
import io.kotest.property.arbitrary.Codepoint
import io.kotest.property.arbitrary.alphanumeric
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.string
import io.kotest.property.arbitrary.uuid
import java.time.LocalDateTime

fun Arb.Companion.categoryEntity() = arbitrary {
    CategoryEntity(
        id = int(min = 1).next(),
        isDeleted = boolean().next(),
        name = string().next(),
    )
}

fun Arb.Companion.partnerEntity() = arbitrary {
    PartnerEntity(
        id = int(min = 1).next(),
        isDeleted = boolean().next(),
        name = string().next(),
        categoryIds = list(int(min = 1), 1..5).next().distinct(),
    )
}

fun Arb.Companion.workoutEntity() = arbitrary {
    WorkoutEntity(
        id = int(min = 1).next(),
        name = string().next(),
        slug = string(minSize = 1, maxSize = 20, codepoints = Codepoint.alphanumeric()).next(),
        start = LocalDateTime.now(),
        end = LocalDateTime.now(),
        partnerId = int(min = 1).next(),
    )
}

fun Arb.Companion.reservationEntity() = arbitrary {
    ReservationEntity(
        uuid = uuid().next(),
        workoutId = int(min = 1).next(),
        workoutStart = LocalDateTime.now(),
    )
}
