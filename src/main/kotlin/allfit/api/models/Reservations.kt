@file:UseSerializers(ZonedDateTimeSerializer::class)

package allfit.api.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import java.time.ZonedDateTime

@Serializable
data class ReservationsJson(
    val data: List<ReservationJson>
)

@Serializable
data class ReservationJson(
    val uuid: String,
    val created_at: ZonedDateTime,
    val workout: WorkoutReservationJson,
)

@Serializable
data class WorkoutReservationJson(
    val id: Int,
    val partner: WorkoutReservationPartnerJson
)

@Serializable
data class WorkoutReservationPartnerJson(
    val id: Int,
)
