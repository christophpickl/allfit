package allfit.sync

import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.string

fun Arb.Companion.workoutFetch() = arbitrary {
    WorkoutFetch(
        workoutId = int().next(),
        about = string().next(),
        specifics = string().next(),
        address = string().next(),
        imageUrls = list(string(), 0..5).next()
    )
}
