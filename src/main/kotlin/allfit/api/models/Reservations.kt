@file:UseSerializers(ZonedDateTimeSerializer::class)

package allfit.api.models

import java.time.ZonedDateTime
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

@Serializable
data class ReservationsJsonRoot(
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
    val name: String,
    val slug: String,
    val from: ZonedDateTime,
    val till: ZonedDateTime,
    val partner: WorkoutReservationPartnerJson,
)

@Serializable
data class WorkoutReservationPartnerJson(
    val id: Int,
)
