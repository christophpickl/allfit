@file:UseSerializers(ZonedDateTimeSerializer::class)

package allfit.api.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import java.time.ZonedDateTime

@Serializable
data class WorkoutsJsonRoot(
    override val data: List<WorkoutJson>,
    override val meta: MetaJson,
) : PagedJson<WorkoutJson>

@Serializable
data class WorkoutJson(
    override val id: Int,
    val name: String,
    val slug: String, // to create the web address
    val partner: WorkoutPartnerJson,
    val location: WorkoutLocationJson,
    val from: ZonedDateTime,
    val till: ZonedDateTime,
    val spots_available: Int, // needs a continuous update...
    val waitlist: Boolean,
    val reservation_allowed: Boolean,
    val is_digital: Boolean,
    // spots_available: Int,
    // spots_booked: Int,
    // workout_type: String, // enum: lesson_committed, ...
    // reserved: Boolean,

) : SyncableJson

@Serializable
data class WorkoutPartnerJson(
    val id: Int,
)

// slightly different from PartnerLocationJson
@Serializable
data class WorkoutLocationJson(
    val street: String,
    val house_number: String,
    val addition: String,
    val zip_code: String?,
    val city: String,
    val latitude: Double,
    val longitude: Double,
)

@Serializable
data class SingleWorkoutJsonRoot(
    val data: SingleWorkoutDataJson,
)

@Serializable
data class SingleWorkoutDataJson(
    val id: Int,
    val name: String,
    val slug: String,
    val description: String,
    val from: ZonedDateTime,
    val till: ZonedDateTime,
    val partner: PartnerWorkoutJson,
)

@Serializable
data class PartnerWorkoutJson(
    val id: Int,
    val name: String,
    val slug: String,
    val description: String,
    val specifics: String,
    val facilities: List<String>,
    val activities: List<String>,
    val drop_in_allowed: Boolean,
    // location ...
)
