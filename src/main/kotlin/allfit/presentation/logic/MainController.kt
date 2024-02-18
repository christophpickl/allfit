package allfit.presentation.logic

import allfit.presentation.ApplicationStartedFxEvent
import allfit.presentation.PartnerWorkoutSelectedFXEvent
import allfit.presentation.SelectTabFXEvent
import allfit.presentation.WorkoutSearchFXEvent
import allfit.presentation.WorkoutSelectedFXEvent
import allfit.presentation.WorkoutSelectedThrough
import allfit.presentation.models.UsageModel
import allfit.presentation.partners.PartnersViewModel
import allfit.presentation.tornadofx.safeSubscribe
import allfit.presentation.view.MainView
import allfit.presentation.view.VersionMismatchDialog
import allfit.presentation.workouts.WorkoutsSearchView
import allfit.presentation.workouts.WorkoutsViewModel
import allfit.service.MetaProps
import allfit.service.VersionChecker
import allfit.service.VersionResult
import io.github.oshai.kotlinlogging.KotlinLogging.logger
import kotlinx.coroutines.runBlocking
import tornadofx.Controller
import tornadofx.runLater

class MainController : Controller() {

    private val logger = logger {}

    private val partnersModel: PartnersViewModel by inject()
    private val workoutsModel: WorkoutsViewModel by inject()
    private val usageModel: UsageModel by inject()
    private val mainView: MainView by inject()
    private val workoutsSearchView: WorkoutsSearchView by inject()

    private val dataStorage: DataStorage by di()
    private val versionChecker: VersionChecker by di()

    init {
        safeSubscribe<ApplicationStartedFxEvent>() {
            logger.debug { "Application started." }
            versionCheck()
            workoutsSearchView.checkSearch() // will fire WorkoutSearchFXEvent
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
                val usage = usageModel.usage.get()
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

    private fun versionCheck() {
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
}
