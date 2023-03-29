package allfit.api

object OnefitUtils {
    fun partnerUrl(id: Int, slug: String) = "https://one.fit/en-nl/partners/$id/$slug"
    fun workoutUrl(id: Int, slug: String) = "https://one.fit/en-nl/workouts/$id/$slug"
}
