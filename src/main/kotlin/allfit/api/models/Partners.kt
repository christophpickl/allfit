@file:Suppress("PropertyName")

package allfit.api.models

import kotlinx.serialization.Serializable

@Serializable
data class PartnersJsonRoot(
    override val data: List<PartnerJson>
) : SyncableJsonContainer<PartnerJson>

@Serializable
data class PartnerJson(
    override val id: Int,
    val name: String,
    val slug: String,
    val description: String,
    val category: PartnerCategoryJson,
    val categories: List<PartnerSubCategoryJson>,
    val header_image: HeaderImageJson? = null,
    val settlement_options: SettlementOptionsJson,
    val location_groups: List<PartnerLocationGroupsJson>,
    val facilities: List<String>,
) : SyncableJson

@Serializable
data class PartnerCategoryJson(
    override val id: Int,
    override val name: String,
    override val slugs: SlugJson
) : CategoryJsonDefinition

@Serializable
data class PartnerSubCategoryJson(
    override val id: Int,
    override val name: String,
    override val slugs: SlugJson? = null // actually not part of the JSON, but for pragmatic reasons in the interface!
) : CategoryJsonDefinition

@Serializable
data class HeaderImageJson(
    // https://edge.one.fit/image/partner/image/16280/b7ad750d-8e00-40cb-b590-a6e9c4875d91.jpg
    // add "?w=123" to resize by width
    val orig: String,
)

@Serializable
data class PartnerLocationGroupsJson(
    val latitude: Double,
    val longitude: Double,
    val locations: List<PartnerLocationJson>
)

// slightly different from WorkoutLocationJson
@Serializable
data class PartnerLocationJson(
    val id: String,
    val partner_id: Int,
    val street_name: String,
    val house_number: String,
    val addition: String,
    val zip_code: String,
    val city: String,
    val latitude: Double,
    val longitude: Double,
)

@Serializable
data class SettlementOptionsJson(
    val drop_in_enabled: Boolean,
    val reservable_workouts: Boolean,
    val first_come_first_serve: Boolean,
)
/*
{
  "data": [
    {
      "id": 16280,
      "name": "Rocycle Amsterdam - City",
      "slug": "rocycle-amsterdam-city-amsterdam",
      "facilities": [
        "Airco",
        "Douche",
        "Kleedkamers",
        "Lockers"
      ],
      "review_rating": 4.73,
      "review_count": 5354,
      "description": "Rocycle maakt van je work-out een feestje op de fiets! In hun boutique sportlocatie fiets je in een met kaarsen verlichte studio, de beats knallen uit de speakers en je traint je bovenlichaam met dumbbells. Ondertussen strooit je instructeur met positieve vibes als confetti. Na 45 minuten knallen verlaat je opgeladen de zaal en kan je jezelf heerlijk terugtrekken in de luxe kleedkamers. Rocycle heeft meerdere prachtige locaties. Let\\u2019s party!",
      "is_favorite": false,
      "settlement_options": {
        "drop_in_enabled": false,
        "reservable_workouts": true,
        "first_come_first_serve": false
      },
      "distance_from_position": 0,
      "campaign": true,
      "is_exclusive": false,
      "is_new": false,
      "category": {
        "id": 1,
        "name": "Gym",
        "slugs": {
          "nl": "gym",
          "en": "gym",
          "es": "gym"
        }
      },
      "categories": [
        {
          "id": 46,
          "name": "Spinning"
        }
      ],
      "header_image": {
        "desktop": "https:\\/\\/edge.one.fit\\/image\\/partner\\/image\\/16280\\/b7ad750d-8e00-40cb-b590-a6e9c4875d91.jpg?w=1680",
        "xxxhdpi": "https:\\/\\/edge.one.fit\\/image\\/partner\\/image\\/16280\\/b7ad750d-8e00-40cb-b590-a6e9c4875d91.jpg?w=1440",
        "xxhdpi": "https:\\/\\/edge.one.fit\\/image\\/partner\\/image\\/16280\\/b7ad750d-8e00-40cb-b590-a6e9c4875d91.jpg?w=1080",
        "tablet": "https:\\/\\/edge.one.fit\\/image\\/partner\\/image\\/16280\\/b7ad750d-8e00-40cb-b590-a6e9c4875d91.jpg?w=1024",
        "mobile": "https:\\/\\/edge.one.fit\\/image\\/partner\\/image\\/16280\\/b7ad750d-8e00-40cb-b590-a6e9c4875d91.jpg?w=768",
        "xhdpi": "https:\\/\\/edge.one.fit\\/image\\/partner\\/image\\/16280\\/b7ad750d-8e00-40cb-b590-a6e9c4875d91.jpg?w=720",
        "hdpi": "https:\\/\\/edge.one.fit\\/image\\/partner\\/image\\/16280\\/b7ad750d-8e00-40cb-b590-a6e9c4875d91.jpg?w=540",
        "mdpi": "https:\\/\\/edge.one.fit\\/image\\/partner\\/image\\/16280\\/b7ad750d-8e00-40cb-b590-a6e9c4875d91.jpg?w=480",
        "ldpi": "https:\\/\\/edge.one.fit\\/image\\/partner\\/image\\/16280\\/b7ad750d-8e00-40cb-b590-a6e9c4875d91.jpg?w=320",
        "thumbnail": "https:\\/\\/edge.one.fit\\/image\\/partner\\/image\\/16280\\/b7ad750d-8e00-40cb-b590-a6e9c4875d91.jpg?w=150",
        "orig": "https:\\/\\/edge.one.fit\\/image\\/partner\\/image\\/16280\\/b7ad750d-8e00-40cb-b590-a6e9c4875d91.jpg"
      },
      "location_groups": [
        {
          "latitude": 52.36655199999999,
          "longitude": 4.878497,
          "locations": [
            {
              "id": "7302",
              "partner_id": 16280,
              "street_name": "Nieuwe Passeerdersstraat",
              "house_number": "12",
              "addition": "",
              "zip_code": "1016 XP",
              "city": "Amsterdam",
              "latitude": 52.366552,
              "longitude": 4.878497
            }
          ]
        }
      ],
      "awards": [
        {
          "name": "OneFit Award 2022",
          "icon": "https:\\/\\/onefitcdn.nl\\/img\\/specifics\\/awards\\/award-icon-2022.png",
          "date": "2022-01-01T00:00:00+01:00",
          "type": "general"
        }
      ]
    },
 */