package allfit.presentation.logic

import allfit.presentation.models.FullWorkout
import allfit.presentation.view.NumericOperator
import java.time.ZonedDateTime

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

data class FavoriteSearchRequest(
    val operand: Boolean,
) : SubSearchRequest {
    override val predicate: (FullWorkout) -> Boolean = { workout ->
        workout.partner.isFavorited == operand
    }
}

data class DateSearchRequest(
    val operand: ZonedDateTime,
) : SubSearchRequest {
    override val predicate: (FullWorkout) -> Boolean = { workout ->
        workout.simpleWorkout.date.start.dayOfYear == operand.dayOfYear
    }
}
