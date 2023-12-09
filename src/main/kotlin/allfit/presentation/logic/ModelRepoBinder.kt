package allfit.presentation.logic

import allfit.persistence.domain.ReservationsRepo
import allfit.persistence.domain.UsageRepository
import allfit.presentation.models.DateRange
import allfit.presentation.models.FullPartner
import allfit.presentation.models.FullWorkout
import allfit.presentation.models.PartnersViewModel
import allfit.presentation.models.Usage
import allfit.presentation.models.UsageModel
import allfit.presentation.models.toUsage
import allfit.presentation.search.GeneralWorkoutFilter
import allfit.presentation.search.GeneralWorkoutSearchRequest
import allfit.presentation.workouts.WorkoutsMainModel
import allfit.service.Clock
import allfit.service.fromUtcToAmsterdamZonedDateTime
import io.github.oshai.kotlinlogging.KotlinLogging.logger
import tornadofx.Controller
import tornadofx.toObservable

class ModelRepoBinder : Controller() {

    private val logg = logger {}
    private val clock: Clock by di()

    // TODO no one in presentation package should access REPO layer; ONLY go through dataStorage (as IT will be mocked out during development)
    private val usageRepo: UsageRepository by di()
    private val reservationsRepo: ReservationsRepo by di()
    private val usageModel: UsageModel by inject()
    private val workoutsModel: WorkoutsMainModel by inject()
    private val partnersModel: PartnersViewModel by inject()
    private val dataStorage: DataStorage by di()

    fun bindModels() {
        logg.info { "Binding models ..." }

        val usage = bindUsage()

        workoutsModel.selectedPartner.initPartner(FullPartner.prototype, usage)
        workoutsModel.selectedWorkout.set(FullWorkout.prototype)

        bindWorkouts()
        bindPartners()
    }

    private fun bindUsage(): Usage {
        val usage = usageRepo.selectOne().toUsage()
        usageModel.today.set(clock.now())
        usageModel.usage.set(usage)
        val reservations = reservationsRepo.selectAll()
        println("usage period: ${usage.period}")
        usageModel.periodReservations.set(reservations.count {
            usage.period.contains(it.workoutStart.fromUtcToAmsterdamZonedDateTime()).also { x ->
                println("${it.workoutStart.fromUtcToAmsterdamZonedDateTime()} => $x")
            }
        })
        usageModel.totalReservations.set(reservations.count())
        return usage
    }

    private fun bindPartners() {
        partnersModel.selectedPartner.initPartner(FullPartner.prototype, usageModel.usage.get())
        partnersModel.selectedWorkout.set(FullWorkout.prototype)
        partnersModel.allPartners.addAll(dataStorage.getPartners())
    }

    private fun bindWorkouts() {
        val workouts = dataStorage.getWorkouts()
        workoutsModel.allWorkouts.addAll(workouts.toObservable())
        workoutsModel.allGroups.addAll(dataStorage.getCategories())
        val initialWorkoutTimeSearch =
            GeneralWorkoutSearchRequest(GeneralWorkoutFilter.UPCOMING, DateRange.NONE).predicate
        workoutsModel.sortedFilteredWorkouts.predicate = {
            WorkoutsMainModel.DEFAULT_WORKOUT_PREDICATE(it) && initialWorkoutTimeSearch(it)
        }
    }
}