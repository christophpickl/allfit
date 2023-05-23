package allfit.presentation.logic

import allfit.presentation.*
import allfit.presentation.models.FullPartner
import allfit.presentation.models.FullWorkout
import allfit.presentation.models.MainViewModel
import mu.KotlinLogging.logger
import tornadofx.Controller
import tornadofx.toObservable

class MainController : Controller() {

    private val dataStorage: DataStorage by di()
    private val logger = logger {}
    private val mainViewModel: MainViewModel by inject()

    init {
        mainViewModel.selectedPartner.initPartner(FullPartner.prototype)
        mainViewModel.selectedWorkout.set(FullWorkout.prototype)

        subscribe<ApplicationStartedFxEvent>() {
            logger.debug { "Application started." }
            val workouts = dataStorage.getUpcomingWorkouts()
            mainViewModel.allWorkouts.addAll(workouts.toObservable())
            mainViewModel.allGroups.addAll(dataStorage.getCategories())
        }
        subscribe<SearchFXEvent>() {
            logger.debug { "Search: ${it.searchRequest}" }
            mainViewModel.sortedFilteredWorkouts.predicate = it.searchRequest.predicate
        }
        subscribe<WorkoutSelectedFXEvent>() {
            val workout = it.workout
            logger.debug { "Change workout: $workout" }
            mainViewModel.selectedWorkout.set(workout)
            mainViewModel.selectedPartner.initPartner(dataStorage.getPartnerById(workout.partner.id))
        }
        subscribe<PartnerWorkoutSelectedFXEvent>() {
            val workout = it.workout
            logger.debug { "Change workout: $workout" }
            mainViewModel.selectedWorkout.set(dataStorage.getWorkoutById(workout.id))
            // no partner update
        }
        subscribe<UpdatePartnerFXEvent>() {
            logger.debug { "Updating partner: ${it.modifications}" }
            dataStorage.updatePartner(it.modifications)
            mainViewModel.sortedFilteredWorkouts.refilter()
        }
    }
}
