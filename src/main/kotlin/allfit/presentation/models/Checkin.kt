package allfit.presentation.models

import java.time.ZonedDateTime

sealed interface Checkin {
    class WorkoutCheckin(val workout: SimpleWorkout) : Checkin
    class DropinCheckin(val date: ZonedDateTime) : Checkin
}
