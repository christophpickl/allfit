package allfit.presentation

import allfit.presentation.models.FullWorkout
import allfit.presentation.models.SimpleWorkout
import allfit.presentation.search.SearchRequest
import tornadofx.FXEvent

object ApplicationStartedFxEvent : FXEvent()

class SearchFXEvent(val searchRequest: SearchRequest) : FXEvent()

class WorkoutSelectedFXEvent(val workout: FullWorkout) : FXEvent()

class PartnerWorkoutSelectedFXEvent(val workout: SimpleWorkout) : FXEvent()

class UpdatePartnerFXEvent(val modifications: PartnerModifications) : FXEvent()

class HidePartnerFXEvent(val partnerId: Int) : FXEvent()

class UnhidePartnerFXEvent(val partnerId: Int) : FXEvent()
