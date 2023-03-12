package allfit.domain

import java.net.URL
import java.time.ZonedDateTime

data class Workout(
    override val id: Int,
    val name: String,
    val slug: String,
    val start: ZonedDateTime,
    val end: ZonedDateTime,
    val partner: Partner,
) : HasIntId {
    // e.g.: https://one.fit/en-nl/workouts/11002448/vondelgym-zuid-fitness-vegym-training
    val webAddress = URL("https://one.fit/en-nl/workouts/$id/$slug")
}
