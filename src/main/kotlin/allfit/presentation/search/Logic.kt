package allfit.presentation.search

import allfit.presentation.models.FullWorkout
import allfit.presentation.models.MainViewModel

interface SubSearchRequest {
    val predicate: (FullWorkout) -> Boolean
}

object DefaultSubSearchRequest : SubSearchRequest {
    override val predicate: (FullWorkout) -> Boolean = {
        MainViewModel.DEFAULT_WORKOUT_PREDICATE(it)
    }
}

data class SearchRequest(
    val subSearchRequests: Set<SubSearchRequest>
) {
    companion object {
        val empty = SearchRequest(emptySet())
    }

    val predicate: (FullWorkout) -> Boolean = { workout ->
        (subSearchRequests + DefaultSubSearchRequest).all {
            it.predicate(workout)
        }
    }
}
