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
    val type: String, // enum: workout, ...
    val created_at: ZonedDateTime,
    val workout: WorkoutCheckinJson,
    // invalid_reason: X?,
    // checked_out_at: X?,
)

@Serializable
data class WorkoutCheckinJson(
    val id: Int,
    val name: String,
    val slug: String,
    val from: ZonedDateTime,
    val till: ZonedDateTime,
    val partner: PartnerWorkoutCheckinJson,
)

@Serializable
data class PartnerWorkoutCheckinJson(
    val id: Int,
    val name: String,
    val slug: String,
    val category: CategoryJson,
)
