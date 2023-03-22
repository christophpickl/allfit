package allfit.presentation.logic

import allfit.presentation.models.FullWorkout
import allfit.presentation.view.NumericOperator

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

data class CheckinSearchRequest(
    val operand: Int,
    val operator: NumericOperator,
) : SubSearchRequest {
    override val predicate: (FullWorkout) -> Boolean = { workout ->
        operator.comparator(workout.partner.checkins, operand)
    }
}

data class FavouriteSearchRequest(
    val operand: Boolean,
) : SubSearchRequest {
    override val predicate: (FullWorkout) -> Boolean = { workout ->
        workout.partner.isFavourited == operand
    }
}
