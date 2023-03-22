package allfit.presentation.models

interface Workout {
    val id: Int
    val name: String
}

data class SimpleWorkout(
    override val id: Int,
    override val name: String,
// val start: ZonedDateTime,
) : Workout

data class FullWorkout(
    val simpleWorkout: SimpleWorkout,
    val partner: SimplePartner,
) : Workout by simpleWorkout
