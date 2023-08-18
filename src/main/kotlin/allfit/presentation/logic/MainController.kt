package allfit.presentation.logic

import allfit.persistence.domain.ReservationsRepo
import allfit.persistence.domain.UsageRepository
import allfit.presentation.ApplicationStartedFxEvent
import allfit.presentation.PartnerWorkoutSelectedFXEvent
import allfit.presentation.SelectTabFXEvent
import allfit.presentation.WorkoutSearchFXEvent
import allfit.presentation.WorkoutSelectedFXEvent
import allfit.presentation.WorkoutSelectedThrough
import allfit.presentation.models.FullPartner
import allfit.presentation.models.FullWorkout
import allfit.presentation.models.PartnersViewModel
import allfit.presentation.models.UsageModel
import allfit.presentation.models.toUsage
import allfit.presentation.search.VisitedSearchRequest
import allfit.presentation.search.VisitedState
import allfit.presentation.tornadofx.safeSubscribe
import allfit.presentation.view.MainView
import allfit.presentation.view.VersionMismatchDialog
import allfit.presentation.workouts.WorkoutsMainModel
import allfit.service.Clock
import allfit.service.MetaProps
import allfit.service.VersionChecker
import allfit.service.VersionResult
import io.github.oshai.kotlinlogging.KotlinLogging.logger
import kotlinx.coroutines.runBlocking
import tornadofx.Controller
import tornadofx.runLater
import tornadofx.toObservable

class MainController : Controller() {

    private val logger = logger {}

    private val workoutsModel: WorkoutsMainModel by inject()
    private val partnersModel: PartnersViewModel by inject()
    private val usageModel: UsageModel by inject()
    private val mainView: MainView by inject()

    private val dataStorage: DataStorage by di()
    private val usageRepo: UsageRepository by di()
    private val reservationsRepo: ReservationsRepo by di()
    private val clock: Clock by di()
    private val versionChecker: VersionChecker by di()

    init {
        val usage = usageRepo.selectOne().toUsage()
        workoutsModel.selectedPartner.initPartner(FullPartner.prototype, usage)
        workoutsModel.selectedWorkout.set(FullWorkout.prototype)
        usageModel.today.set(clock.now())
        usageModel.usage.set(usage)
        usageModel.reservations.set(reservationsRepo.selectAll().count())

        safeSubscribe<ApplicationStartedFxEvent>() {
            logger.debug { "Application started." }
            val workouts = dataStorage.getWorkouts()
            workoutsModel.allWorkouts.addAll(workouts.toObservable())
            workoutsModel.allGroups.addAll(dataStorage.getCategories())
            val initialWorkoutTimeSearch = VisitedSearchRequest(VisitedState.UPCOMING).predicate
            workoutsModel.sortedFilteredWorkouts.predicate = {
                WorkoutsMainModel.DEFAULT_WORKOUT_PREDICATE(it) && initialWorkoutTimeSearch(it)
            }

            runAsync {
                val versionResult = runBlocking {
                    versionChecker.check(MetaProps.instance.version)
                }
                runLater {
                    if (versionResult is VersionResult.TooOld) {
                        VersionMismatchDialog(
                            currentVersion = versionResult.currentVersion, latestVersion = versionResult.latestVersion
                        ).openModal()
                    }
                }
            }
        }
        safeSubscribe<WorkoutSearchFXEvent>() {
            logger.debug { "Search: ${it.searchRequest}" }
            workoutsModel.sortedFilteredWorkouts.predicate = it.searchRequest.predicate
        }
        safeSubscribe<WorkoutSelectedFXEvent>() {
            val workout = it.workout
            logger.debug { "Change workout selected: $workout" }
            workoutsModel.selectedWorkout.set(workout)
            if (workoutsModel.selectedPartner.id.get() != workout.partner.id) {
                workoutsModel.selectedPartner.initPartner(dataStorage.getPartnerById(workout.partner.id), usage)
            }
        }
        safeSubscribe<PartnerWorkoutSelectedFXEvent>() { event ->
            val workout = event.workout
            logger.debug { "Change partner workout selected: $workout (${event.selectedThrough})" }
            val fullWorkout = dataStorage.getFullWorkoutById(workout.id)
            when (event.selectedThrough) {
                WorkoutSelectedThrough.Workouts -> workoutsModel.selectedWorkout.set(fullWorkout)
                WorkoutSelectedThrough.Partners -> partnersModel.selectedWorkout.set(fullWorkout)
            }
        }
        safeSubscribe<SelectTabFXEvent>() { event ->
            mainView.tabPane.selectionModel.select(mainView.tabPane.tabs[event.tab.number])
        }
        // no partner update
    }
}
