package allfit.domain

import java.net.URL

data class Workout(
    val id: Int,
    val name: String,
    val slug: String,
) {
    // e.g.: https://one.fit/en-nl/workouts/11002448/vondelgym-zuid-fitness-vegym-training
    val webAddress = URL("https://one.fit/en-nl/workouts/$id/$slug")
}
