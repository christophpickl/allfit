package allfit.sync

import allfit.service.readHtmlResponse
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class WorkoutHtmlParserTest : StringSpec() {
    private val workoutId = 40
    private val anyWorkoutId = 42

    init {
        "When parse HTML Then return relevant data" {
            val workout = WorkoutHtmlParser.parse(workoutId, readHtmlResponse("workout.html"))
            workout shouldBe WorkoutFetch(
                workoutId = workoutId,
                about = "about1<br>\n about2",
                specifics = "specifics1 <br><a href=\"foo\">specifics2</a>",
                address = "Workout Address",
                imageUrls = listOf(
                    "https://edge.one.fit/image/partner/image/17239/e8c44e8e-07d3-42be-afa6-95d7b91db85b.jpg",
                    "https://edge.one.fit/image/partner/image/17239/056a8af1-baa6-40cd-962a-1bb2b9724541.jpg",
                ),
            )
        }
        "When parse HTML without about Then return empty string" {
            val workout = WorkoutHtmlParser.parse(anyWorkoutId, readHtmlResponse("workout-no_about.html"))
            workout.about shouldBe ""
        }
        "When parse HTML without specifics Then return empty string" {
            val workout = WorkoutHtmlParser.parse(workoutId, readHtmlResponse("workout-no_specifics.html"))
            workout.specifics shouldBe ""
        }
    }
}
