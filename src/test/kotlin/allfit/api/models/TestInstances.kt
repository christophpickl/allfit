package allfit.api.models

import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.string

fun Arb.Companion.partnerCategoryJson() = arbitrary {
    PartnerCategoryJson(
        id = int(min = 1).next(),
        name = string(minSize = 1, maxSize = 8).next(),
        slugs = slugJson().next()
    )
}

fun Arb.Companion.slugJson() = arbitrary {
    SlugJson(
        nl = string(minSize = 1, maxSize = 8).next(),
        en = string(minSize = 1, maxSize = 8).next(),
        es = string(minSize = 1, maxSize = 8).next(),
    )
}
