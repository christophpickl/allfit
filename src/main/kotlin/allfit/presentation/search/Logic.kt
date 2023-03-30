package allfit.presentation.search

import allfit.presentation.models.FullWorkout

interface SubSearchRequest {
    val predicate: (FullWorkout) -> Boolean
}

data class SearchRequest(
    val subSearchRequests: Set<SubSearchRequest>
) {
    companion object {
        val empty = SearchRequest(emptySet())
    }

    val predicate: (FullWorkout) -> Boolean = { workout ->
        subSearchRequests.all {
            it.predicate(workout)
        }
    }
}
