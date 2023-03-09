package allfit.api.models

import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.orNull
import io.kotest.property.arbitrary.string

fun Arb.Companion.categoryJson() = arbitrary {
    CategoryJson(
        id = int(min = 1).next(),
        name = string(minSize = 1, maxSize = 8).next(),
        slugs = slugJson().next()
    )
}

fun Arb.Companion.slugJson() = arbitrary {
    SlugJson(
        en = string(minSize = 1, maxSize = 8).next(),
        nl = string(minSize = 1, maxSize = 8).orNull().next(),
        es = string(minSize = 1, maxSize = 8).orNull().next(),
    )
}

fun Arb.Companion.partnerJson() = arbitrary {
    PartnerJson(
        id = int(min = 1).next(),
        name = string(minSize = 1, maxSize = 8).next(),
        category = partnerCategoryJson().next(),
        categories = list(partnerCategoryJson(), 0..5).next(),
    )
}

fun Arb.Companion.partnerCategoryJson() = arbitrary {
    PartnerCategoryJson(
        id = int(min = 1).next(),
        name = string(minSize = 1, maxSize = 8).next(),
    )
}