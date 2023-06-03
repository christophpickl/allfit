package allfit.presentation.logic

import allfit.presentation.ApplicationStartedFxEvent
import allfit.presentation.HidePartnerFXEvent
import allfit.presentation.PartnerWorkoutSelectedFXEvent
import allfit.presentation.SearchFXEvent
import allfit.presentation.UnhidePartnerFXEvent
import allfit.presentation.WorkoutSelectedFXEvent
import allfit.presentation.models.FullPartner
import allfit.presentation.models.FullWorkout
import allfit.presentation.models.MainViewModel
import allfit.presentation.models.PartnersViewModel
import allfit.presentation.tornadofx.safeSubscribe
import mu.KotlinLogging.logger
import tornadofx.Controller
import tornadofx.toObservable

class MainController : Controller() {

    private val dataStorage: DataStorage by di()
    private val logger = logger {}
    private val mainViewModel: MainViewModel by inject()
    private val partnersViewModel: PartnersViewModel by inject()

    init {
        mainViewModel.selectedPartner.initPartner(FullPartner.prototype)
        mainViewModel.selectedWorkout.set(FullWorkout.prototype)

        safeSubscribe<ApplicationStartedFxEvent>() {
            logger.debug { "Application started." }
            val workouts = dataStorage.getUpcomingWorkouts()
            mainViewModel.allWorkouts.addAll(workouts.toObservable())
            mainViewModel.allWorkouts.addAll(workouts.toObservable())
            mainViewModel.allGroups.addAll(dataStorage.getCategories())
            partnersViewModel.allRawPartners.addAll(dataStorage.getPartners())
            mainViewModel.sortedFilteredWorkouts.predicate = MainViewModel.DEFAULT_WORKOUT_PREDICATE
        }
        safeSubscribe<SearchFXEvent>() {
            logger.debug { "Search: ${it.searchRequest}" }
            mainViewModel.sortedFilteredWorkouts.predicate = it.searchRequest.predicate
        }
        safeSubscribe<WorkoutSelectedFXEvent>() {
            val workout = it.workout
            logger.debug { "Change workout: $workout" }
            mainViewModel.selectedWorkout.set(workout)
            mainViewModel.selectedPartner.initPartner(dataStorage.getPartnerById(workout.partner.id))
        }
        safeSubscribe<PartnerWorkoutSelectedFXEvent>() {
            val workout = it.workout
            logger.debug { "Change workout: $workout" }
            mainViewModel.selectedWorkout.set(dataStorage.getWorkoutById(workout.id))
            // no partner update
        }
        safeSubscribe<HidePartnerFXEvent>() {
            logger.debug { "Received HidePartnerFXEvent: ${it.partnerId}" }
            dataStorage.hidePartner(it.partnerId)
            mainViewModel.sortedFilteredWorkouts.refilter()
        }
        safeSubscribe<UnhidePartnerFXEvent>() {
            logger.debug { "Received UnhidePartnerFXEvent: ${it.partnerId}" }
            dataStorage.unhidePartner(it.partnerId)
            mainViewModel.sortedFilteredWorkouts.refilter()
        }
    }
}
