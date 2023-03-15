package allfit

import io.kotest.property.Arb
import io.kotest.property.arbitrary.Codepoint
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.of
import io.kotest.property.arbitrary.string
import java.util.UUID

val uuid1: UUID = UUID.fromString("00000000-0000-0000-0000-000000000001")
val uuid2: UUID = UUID.fromString("00000000-0000-0000-0000-000000000002")

fun Codepoint.Companion.numeric(): Arb<Codepoint> =
    Arb.of(('0'..'9').map { Codepoint(it.code) })

fun Arb.Companion.intAsString() = arbitrary {
    string(minSize = 1, maxSize = 9, codepoints = Codepoint.numeric()).next().also {
        it.toInt() // sanity check, below INT.MAX_VALUE of 2147483647
    }
}
