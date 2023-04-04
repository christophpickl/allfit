package allfit.persistence.testInfra

import allfit.persistence.domain.ReservationEntity
import allfit.persistence.domain.WorkoutEntity
import java.time.LocalDateTime

fun WorkoutEntity.withFutureStart(): WorkoutEntity {
    val future = LocalDateTime.now().plusDays(1)
    return copy(
        start = future,
        end = future.plusHours(1),
    )
}

fun ReservationEntity.withFutureStart(): ReservationEntity = copy(
    workoutStart = LocalDateTime.now().plusDays(1)
)
