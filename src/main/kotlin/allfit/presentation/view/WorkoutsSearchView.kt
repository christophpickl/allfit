package allfit.presentation.view

import allfit.presentation.WorkoutSearchFXEvent
import allfit.presentation.models.FullWorkout
import allfit.presentation.models.MainViewModel
import allfit.presentation.search.DateSearchPane
import allfit.presentation.search.FavoriteSearchPane
import allfit.presentation.search.GroupSearchPane
import allfit.presentation.search.IsWishlistedSearchPane
import allfit.presentation.search.IsWorkoutReservedSearchPane
import allfit.presentation.search.PartnerCheckinSearchPane
import allfit.presentation.search.SearchRequest
import allfit.presentation.search.SearchView
import allfit.presentation.search.SubSearchRequest
import allfit.presentation.search.TextSearchPane
import allfit.service.Clock
import tornadofx.FXEvent
import tornadofx.hbox
import tornadofx.vbox

private object DefaultSubSearchRequest : SubSearchRequest<FullWorkout> {
    override val predicate: (FullWorkout) -> Boolean = {
        MainViewModel.DEFAULT_WORKOUT_PREDICATE(it)
    }
}

class WorkoutsSearchView : SearchView<FullWorkout>(DefaultSubSearchRequest) {

    private val clock: Clock by di()

    override val root = hbox {
        vbox {
            addIt(TextSearchPane(::checkSearch))
            addIt(DateSearchPane(clock, ::checkSearch))
            addIt(GroupSearchPane(::checkSearch))
            addIt(PartnerCheckinSearchPane(::checkSearch))
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
