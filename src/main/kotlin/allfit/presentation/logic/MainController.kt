package allfit.presentation.logic

import allfit.persistence.domain.UsageRepository
import allfit.presentation.ApplicationStartedFxEvent
import allfit.presentation.PartnerWorkoutSelectedFXEvent
import allfit.presentation.WorkoutSearchFXEvent
import allfit.presentation.WorkoutSelectedFXEvent
import allfit.presentation.models.FullPartner
import allfit.presentation.models.FullWorkout
import allfit.presentation.models.MainViewModel
import allfit.presentation.models.PartnersViewModel
import allfit.presentation.models.UsageModel
import allfit.presentation.models.toUsage
import allfit.presentation.tornadofx.safeSubscribe
import allfit.service.Clock
import mu.KotlinLogging.logger
import tornadofx.Controller
import tornadofx.toObservable

class MainController : Controller() {

    private val logger = logger {}

    private val mainViewModel: MainViewModel by inject()
    private val partnersViewModel: PartnersViewModel by inject()
    private val usageModel: UsageModel by inject()

    private val dataStorage: DataStorage by di()
    private val usageRepo: UsageRepository by di()
    private val clock: Clock by di()

    init {
        val usage = usageRepo.selectOne().toUsage()
        mainViewModel.selectedPartner.initPartner(FullPartner.prototype, usage)
        mainViewModel.selectedWorkout.set(FullWorkout.prototype)
        usageModel.today.set(clock.now())
        usageModel.usage.set(usage)

        safeSubscribe<ApplicationStartedFxEvent>() {
            logger.debug { "Application started." }
            val workouts = dataStorage.getUpcomingWorkouts()
            mainViewModel.allWorkouts.addAll(workouts.toObservable())
            mainViewModel.allGroups.addAll(dataStorage.getCategories())
            partnersViewModel.allPartners.addAll(dataStorage.getPartners())
            mainViewModel.sortedFilteredWorkouts.predicate = MainViewModel.DEFAULT_WORKOUT_PREDICATE

        }
        safeSubscribe<WorkoutSearchFXEvent>() {
            logger.debug { "Search: ${it.searchRequest}" }
            mainViewModel.sortedFilteredWorkouts.predicate = it.searchRequest.predicate
        }
        safeSubscribe<WorkoutSelectedFXEvent>() {
            val workout = it.workout
            logger.debug { "Change workout: $workout" }
            mainViewModel.selectedWorkout.set(workout)
            mainViewModel.selectedPartner.initPartner(dataStorage.getPartnerById(workout.partner.id), usage)
        }
        safeSubscribe<PartnerWorkoutSelectedFXEvent>() {
            val workout = it.workout
            logger.debug { "Change workout: $workout" }
            mainViewModel.selectedWorkout.set(dataStorage.getWorkoutById(workout.id))
            // no partner update
        }
    }
}
