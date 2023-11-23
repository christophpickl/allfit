package allfit.presentation

import allfit.presentation.models.FullPartner
import allfit.presentation.models.FullWorkout
import allfit.presentation.models.SimpleWorkout
import allfit.presentation.partners.PartnerModifications
import allfit.presentation.search.SearchRequest
import tornadofx.FXEvent

object ApplicationStartedFxEvent : FXEvent()
object ApplicationStoppingFxEvent : FXEvent()

class WorkoutSearchFXEvent(val searchRequest: SearchRequest<FullWorkout>) : FXEvent()

class WorkoutSelectedFXEvent(val workout: FullWorkout) : FXEvent()

class PartnerWorkoutSelectedFXEvent(
    val workout: SimpleWorkout,
    val selectedThrough: WorkoutSelectedThrough,
) : FXEvent()

enum class WorkoutSelectedThrough {
    Workouts, Partners;
}

class UpdatePartnerFXEvent(val modifications: PartnerModifications) : FXEvent()

class HidePartnerFXEvent(val partnerId: Int) : FXEvent()

class UnhidePartnerFXEvent(val partnerId: Int) : FXEvent()

class PartnerSelectedFXEvent(val partnerId: Int) : FXEvent()

class PartnerSearchFXEvent(val searchRequest: SearchRequest<FullPartner>) : FXEvent()
