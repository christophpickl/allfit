package allfit.presentation.models

import java.time.ZonedDateTime

sealed interface Checkin {
    val date: ZonedDateTime

    class WorkoutCheckin(
        val workout: SimpleWorkout
    ) : Checkin {
        override val date = workout.date.start
    }

    class DropinCheckin(
        override val date: ZonedDateTime
    ) : Checkin
}
