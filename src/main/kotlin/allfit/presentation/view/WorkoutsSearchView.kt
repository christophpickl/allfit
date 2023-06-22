package allfit.presentation.view

import allfit.presentation.WorkoutSearchFXEvent
import allfit.presentation.models.FullWorkout
import allfit.presentation.models.MainViewModel
import allfit.presentation.search.CheckinSearchPane
import allfit.presentation.search.DateSearchPane
import allfit.presentation.search.FavoriteSearchPane
import allfit.presentation.search.GroupSearchPane
import allfit.presentation.search.IsWishlistedSearchPane
import allfit.presentation.search.IsWorkoutReservedSearchPane
import allfit.presentation.search.RatingSearchPane
import allfit.presentation.search.SearchRequest
import allfit.presentation.search.SearchView
import allfit.presentation.search.SubSearchRequest
import allfit.presentation.search.TextSearchPane
import allfit.presentation.search.VisitedSearchPane
import allfit.service.Clock
import tornadofx.FXEvent
import tornadofx.hbox
import tornadofx.vbox

private object DefaultWorkoutSubSearchRequest : SubSearchRequest<FullWorkout> {
    override val predicate: (FullWorkout) -> Boolean = {
        MainViewModel.DEFAULT_WORKOUT_PREDICATE(it)
    }

    override fun toString() = this::class.simpleName!!
}

class WorkoutsSearchView : SearchView<FullWorkout>(DefaultWorkoutSubSearchRequest) {

    private val clock: Clock by di()

    override val root = hbox {
        vbox {
            addIt(VisitedSearchPane(::checkSearch))
            addIt(TextSearchPane(::checkSearch))
            addIt(DateSearchPane(clock, ::checkSearch))
            addIt(GroupSearchPane(::checkSearch))
            addIt(CheckinSearchPane(::checkSearch))
            addIt(RatingSearchPane(::checkSearch))
        }
        vbox {
            addIt(IsWishlistedSearchPane(::checkSearch))
            addIt(FavoriteSearchPane(::checkSearch))
            addIt(IsWorkoutReservedSearchPane(::checkSearch))
        }
    }

    override fun buildEvent(request: SearchRequest<FullWorkout>): FXEvent =
        WorkoutSearchFXEvent(request)

}
