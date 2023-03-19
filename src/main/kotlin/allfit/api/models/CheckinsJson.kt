@file:UseSerializers(ZonedDateTimeSerializer::class)

package allfit.api.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import java.time.ZonedDateTime

@Serializable
data class CheckinsJson(
    override val data: List<CheckinJson>,
    override val meta: MetaJson,
) : PagedJson<CheckinJson>

@Serializable
data class CheckinJson(
    val uuid: String,
    val type: String, // enum: workout
    val created_at: ZonedDateTime,
    val workout: WorkoutCheckinJson, // FIXME when checkin is in past, workout needs to be fetched individually
    // invalid_reason: X?,
    // checkoued_out_at: X?,
)

@Serializable
data class WorkoutCheckinJson(
    val id: Int,
)
