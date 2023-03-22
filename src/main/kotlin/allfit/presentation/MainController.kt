package allfit.presentation

import allfit.presentation.models.MainViewModel
import allfit.service.DataStorage
import mu.KotlinLogging.logger
import tornadofx.Controller
import tornadofx.toObservable

class MainController : Controller() {

    private val dataStorage: DataStorage by di()
    private val logger = logger {}
    private val mainViewModel: MainViewModel by inject()

    init {
        subscribe<ApplicationStartedFxEvent>() {
            logger.debug { "Application started." }
            val workouts = dataStorage.getAllFullWorkouts()
            mainViewModel.allWorkouts.addAll(workouts.toObservable())
        }
        subscribe<SearchFXEvent>() {
            logger.debug { "Search." }
        }
        subscribe<WorkoutSelectedFXEvent>() {
            logger.debug { "Workout selected: ${it.workout}" }
            mainViewModel.selectedWorkout.set(it.workout)
            mainViewModel.selectedPartner.initPartner(dataStorage.getFullPartnerById(it.workout.partner.id))
        }
        subscribe<SavePartnerFXEvent>() {
            logger.debug { "Saving partner: ${it.modifications}" }
            dataStorage.updatePartner(it.modifications)
        }
    }
}
