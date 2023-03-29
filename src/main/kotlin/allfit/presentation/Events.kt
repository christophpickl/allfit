package allfit.presentation

import allfit.presentation.logic.SearchRequest
import allfit.presentation.models.FullWorkout
import allfit.presentation.models.SimpleWorkout
import tornadofx.FXEvent

object ApplicationStartedFxEvent : FXEvent()

class SearchFXEvent(val searchRequest: SearchRequest) : FXEvent()

class WorkoutSelectedFXEvent(val workout: FullWorkout) : FXEvent()

class PartnerWorkoutSelectedFXEvent(val workout: SimpleWorkout) : FXEvent()

class UpdatePartnerFXEvent(val modifications: PartnerModifications) : FXEvent()
