package allfit.domain

import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.string

fun Arb.Companion.category() = arbitrary {
    Category(
        id = int(min = 1).next(),
        shortCode = string(minSize = 1, maxSize = 8).next(),
    )
}
