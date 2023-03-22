package allfit.presentation

import allfit.presentation.models.FullWorkout
import tornadofx.FXEvent

object ApplicationStartedFxEvent : FXEvent()

object SearchFXEvent : FXEvent()

class WorkoutSelectedFXEvent(val workout: FullWorkout) : FXEvent()

class SavePartnerFXEvent(val modifications: PartnerModifications) : FXEvent()
