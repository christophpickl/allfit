@file:UseSerializers(ZonedDateTimeSerializer::class)

package allfit.api.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import java.time.ZonedDateTime

@Serializable
data class WorkoutsJson(
    override val data: List<WorkoutJson>,
    override val meta: MetaJson,
) : PagedJson<WorkoutJson>

@Serializable
data class WorkoutJson(
    val id: Int,
    val name: String,
    val slug: String, // to create the web address
    val partner: WorkoutPartnerJson,
    val location: WorkoutLocationJson,
    val from: ZonedDateTime,
    val till: ZonedDateTime,
    val reservation_allowed: Boolean,
//    val spots_available: Int, ... would need a continuous update...

// for more data, would need to parse HTML... e.g. from:
// https://one.fit/en-nl/workouts/11002448/vondelgym-zuid-fitness-vegym-training
)

@Serializable
data class WorkoutPartnerJson(
    val id: Int,
)

@Serializable
data class WorkoutLocationJson(
    val street: String,
    val house_number: String,
    val addition: String,
    val zip_code: String,
    val city: String,
    val latitude: Double,
    val longitude: Double,
)

/*
{
  "data": [
    {
      "id": 11038764,
      "name": "Street Jazz",
      "slug": "zhembrovskyy-de-pijp-street-jazz",
      "spots_available": 1,
      "spots_booked": 1,
      "reservation_allowed": true,
      "from": "2023-03-09T21:15:00+01:00",
      "till": "2023-03-09T22:15:00+01:00",
      "reserved": false,
      "waitlist": false,
      "partner": {
        "id": 16608,
        "name": "Zhembrovskyy de Pijp",
        "slug": "zhembrovskyy-de-pijp-amsterdam",
        "waitlist_enabled": true
      },
      "location": {
        "street": "Van Woustraat",
        "house_number": "149",
        "addition": "Hs",
        "zip_code": "1073RX",
        "city": "Amsterdam",
        "latitude": 52.35281372,
        "longitude": 4.90296316
      },
      "attending_friends": [],
      "is_digital": false
    },
 */