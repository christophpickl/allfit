package allfit.sync.domain

import allfit.service.readHtmlResponse
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe

class WorkoutHtmlParserTest : StringSpec() {
    private val workoutId = 40
    private val anyWorkoutId = 42

    init {
        "When parse HTML Then return relevant data" {
            val workout = parse(readHtmlResponse("workout.html"), workoutId)
            workout shouldBe WorkoutFetchMetadata(
                workoutId = workoutId,
                about = "about1<br>\n about2",
                specifics = "specifics1 <br><a href=\"foo\">specifics2</a>",
                address = "Workout Address",
                imageUrls = listOf(
                    "https://edge.one.fit/image/partner/image/17239/e8c44e8e-07d3-42be-afa6-95d7b91db85b.jpg",
                    "https://edge.one.fit/image/partner/image/17239/056a8af1-baa6-40cd-962a-1bb2b9724541.jpg",
                ),
                teacher = null,
            )
        }

        "When parse HTML without about Then return empty string" {
            val workout = parse(readHtmlResponse("workout-no_about.html"))
            workout.about shouldBe ""
        }

        "When parse HTML without specifics Then return empty string" {
            val workout = parse(readHtmlResponse("workout-no_specifics.html"))
            workout.specifics shouldBe ""
        }

        "Given teacher When parse Then find teacher" {
            val workout = parse(readHtmlResponse("workout-teacher_yes.html"))
            workout.teacher shouldBe "My Teacher"
        }

        "Given no teacher When parse Then set teacher as null" {
            val workout = parse(readHtmlResponse("workout-teacher_no.html"))
            workout.teacher.shouldBeNull()
        }
    }

    private fun parse(html: String, workoutId: Int = -42) =
        WorkoutHtmlParser.parse(workoutId, html)
}
