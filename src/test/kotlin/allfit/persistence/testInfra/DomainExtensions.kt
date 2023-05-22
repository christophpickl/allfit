package allfit.persistence.testInfra

import allfit.persistence.domain.ReservationEntity
import allfit.persistence.domain.WorkoutEntity
import java.time.LocalDateTime

fun WorkoutEntity.withFutureStart(now: LocalDateTime): WorkoutEntity {
    val future = now.plusDays(1)
    return copy(
        start = future,
        end = future.plusHours(1),
    )
}

fun ReservationEntity.withFutureStart(now: LocalDateTime): ReservationEntity = copy(
    workoutStart = now.plusDays(1)
)
