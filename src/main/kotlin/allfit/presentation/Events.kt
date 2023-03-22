package allfit.presentation

import allfit.presentation.logic.SearchRequest
import allfit.presentation.models.FullWorkout
import tornadofx.FXEvent

object ApplicationStartedFxEvent : FXEvent()

class SearchFXEvent(val searchRequest: SearchRequest) : FXEvent()

class WorkoutSelectedFXEvent(val workout: FullWorkout) : FXEvent()

class SavePartnerFXEvent(val modifications: PartnerModifications) : FXEvent()
