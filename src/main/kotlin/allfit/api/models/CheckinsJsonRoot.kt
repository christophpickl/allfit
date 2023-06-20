@file:UseSerializers(ZonedDateTimeSerializer::class)
@file:Suppress("PropertyName")

package allfit.api.models

import java.time.ZonedDateTime
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

@Serializable
data class CheckinsJsonRoot(
    override val data: List<CheckinJson>,
    override val meta: MetaJson,
) : PagedJson<CheckinJson>

@Serializable
data class CheckinJson(
    val uuid: String,
    val type: String,
    val created_at: ZonedDateTime,
    val workout: WorkoutCheckinJson? = null,
    val partner: PartnerWorkoutCheckinJson? = null,
    // invalid_reason: X?,
    // checked_out_at: X?,
) {
    companion object {
        const val TYPE_WORKOUT = "workout"
        const val TYPE_DROPIN = "drop-in"
    }

    init {
        when (type) {
            TYPE_WORKOUT -> {
                require(workout != null)
                require(partner == null)
            }

            TYPE_DROPIN -> {
                require(workout == null)
                require(partner != null)
            }

            else -> error("Unsupported type: '$type'")
        }
    }

    val typeSafePartner = when (type) {
        TYPE_WORKOUT -> workout!!.partner
        TYPE_DROPIN -> partner!!
        else -> error("Unsupported type: '$type'")
    }
}

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
