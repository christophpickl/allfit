package allfit.persistence

import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.string

fun Arb.Companion.categoryDbo() = arbitrary {
    CategoryDbo(
        id = int(min = 1).next(),
        name = string(minSize = 1, maxSize = 50).next(),
        isDeleted = boolean().next(),
    )
}

fun Arb.Companion.partnerDbo() = arbitrary {
    PartnerDbo(
        id = int(min = 1).next(),
        name = string(minSize = 1, maxSize = 50).next(),
        isDeleted = boolean().next(),
    )
}
