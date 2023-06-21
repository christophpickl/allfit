package allfit.presentation.models

import allfit.persistence.domain.UsageEntity
import allfit.service.fromUtcToAmsterdamZonedDateTime
import java.time.ZonedDateTime
import javafx.beans.property.SimpleObjectProperty
import tornadofx.ViewModel

data class Usage(
    val total: Int,
    val noShows: Int,
    val from: ZonedDateTime,
    val until: ZonedDateTime, // inclusive that day
    val periodCap: Int, // 16
    val maxCheckInsOrReservationsPerPeriod: Int, // 4 (per partner)
    val totalCheckInsOrReservationsPerDay: Int, // 2
    val maxReservations: Int, // 6 (for whole period)
) {
    val period = DateRange(from, until)

    fun availabilityFor(
        pastCheckins: List<Checkin>,
        upcomingWorkouts: List<SimpleWorkout>,
    ): Int {
        val usedThisPeriod = pastCheckins.count { checkin ->
            when (checkin) {
                is Checkin.WorkoutCheckin -> checkin.workout.date.start in period
                is Checkin.DropinCheckin -> checkin.date in period
            }
        } + upcomingWorkouts.count { it.isReserved && it.date.start in period }
        return maxCheckInsOrReservationsPerPeriod - usedThisPeriod
    }
}

class UsageModel : ViewModel() {
    val usage = SimpleObjectProperty<Usage>()
    val today = SimpleObjectProperty<ZonedDateTime>()
}

fun UsageEntity.toUsage() = Usage(
    total = total,
    noShows = noShows,
    from = from.fromUtcToAmsterdamZonedDateTime(),
    until = until.fromUtcToAmsterdamZonedDateTime(),
    periodCap = periodCap,
    maxCheckInsOrReservationsPerPeriod = maxCheckInsOrReservationsPerPeriod,
    totalCheckInsOrReservationsPerDay = totalCheckInsOrReservationsPerDay,
    maxReservations = maxReservations,
)
