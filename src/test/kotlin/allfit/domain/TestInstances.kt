package allfit.domain

import allfit.service.SystemClock
import io.kotest.property.Arb
import io.kotest.property.arbitrary.Codepoint
import io.kotest.property.arbitrary.alphanumeric
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.string

fun Arb.Companion.category() = arbitrary {
    Category(
        id = int(min = 1).next(),
        name = string(minSize = 1, maxSize = 50).next(),
        isDeleted = boolean().next(),
    )
}

fun Arb.Companion.partner() = arbitrary {
    Partner(
        id = int(min = 1).next(),
        name = string(minSize = 1, maxSize = 50).next(),
        isDeleted = boolean().next(),
        categories = list(category(), 1..4).next(),
    )
}

fun Arb.Companion.workout() = arbitrary {
    Workout(
        id = int(min = 1).next(),
        name = string(minSize = 1, maxSize = 50).next(),
        slug = string(minSize = 1, maxSize = 50, codepoints = Codepoint.alphanumeric()).next(),
        start = SystemClock.now(),
        end = SystemClock.now(),
        partner = partner().next(),
    )
}
