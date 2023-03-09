package allfit.api

import kotlinx.serialization.Serializable

@Serializable
data class AuthJson(
    val email: String,
    val password: String,
)


@Serializable
data class AuthResponseJson(
    val access_token: String,
)

@Serializable
data class UsagesDataJson(
    val reservations: Int,
    val no_shows: Int,
    val check_ins: Int,
    val total: Int,
    val partner_usage: List<PartnerUsageJson>
)

@Serializable
data class PartnerUsageJson(
    val check_ins: Int,
    val partner: PartnerJson
)

@Serializable
data class PartnerJson(
    val id: Int,
    val name: String,
)

@Serializable
data class UsagesJson(
    val data: UsagesDataJson
/*
{
  "data": {
    "reservations": 0,
    "check_ins": 2,
    "surplus_count": 0,
    "included_count": 2,
    "digital": 0,
    "no_shows": 0,
    "total": 2,
    "onsite_total": 2,
    "period": {
      "from": "2023-02-07T00:00:00+01:00",
      "till": "2023-03-09T00:00:00+01:00",
      "display_from": "2023-02-07T00:00:00+01:00",
      "display_till": "2023-03-08T00:00:00+01:00",
      "product": {
        "name": "Start",
        "city": "Amsterdam",
        "category": "business_tier",
        "cycle_length": 1,
        "period_unit": "days",
        "period_amount": 30,
        "translate_key": "b2b_low_tier_product",
        "price": 1250,
        "price_formatted": "€ 12,50",
        "is_default": false,
        "slug": "business_lite_tier",
        "uuid": "QU1TfHxidXNpbmVzc19saXRlX3RpZXI=",
        "onefit_city": "AMS",
        "rollover_product": null,
        "rules": [
          {
            "type": "GeneralPeriodCap",
            "unit": "regular",
            "amount": 4
          },
          {
            "type": "MaxCheckInsOrReservationsPerPeriod",
            "unit": "regular",
            "amount": 2
          },
          {
            "type": "TotalCheckInsOrReservationsPerDay",
            "unit": null,
            "amount": 2
          },
          {
            "type": "MaxCheckInsOrReservationsPerPeriod",
            "unit": "regular",
            "amount": 2
          },
          {
            "type": "OneCheckInOrReservation",
            "unit": null,
            "amount": 1
          },
          {
            "type": "MaxReservations",
            "unit": null,
            "amount": 6
          }
        ],
        "product_features": {
          "surplus_allowed": true,
          "late_cancellation": true,
          "surplus_discount": false,
          "monthly_pause": false
        },
        "mandatory_fields": [],
        "settings": {
          "is_trial": 0,
          "is_digital": 0,
          "allows_discount": 1,
          "is_highlighted": 0
        }
      }
    },
    "partner_usage": [
      {
        "check_ins": 2,
        "reservations": 0,
        "total": 2,
        "onsite_total": 2,
        "surplus_count": 0,
        "included_count": 2,
        "no_shows": 0,
        "digital": 0,
        "partner": {
          "id": 551,
          "name": "Hot Flow Yoga Jordaan",
          "slug": "hot-flow-yoga-jordaan-amsterdam",
          "waitlist_enabled": true,
          "check_in_radius": 75,
          "header_image": {
            "desktop": "https://edge.one.fit/image/partner/image/551/2e3ed679-39f1-4726-83b7-c7395fdc167b.jpg?w=1680",
            "xxxhdpi": "https://edge.one.fit/image/partner/image/551/2e3ed679-39f1-4726-83b7-c7395fdc167b.jpg?w=1440",
            "xxhdpi": "https://edge.one.fit/image/partner/image/551/2e3ed679-39f1-4726-83b7-c7395fdc167b.jpg?w=1080",
            "tablet": "https://edge.one.fit/image/partner/image/551/2e3ed679-39f1-4726-83b7-c7395fdc167b.jpg?w=1024",
            "mobile": "https://edge.one.fit/image/partner/image/551/2e3ed679-39f1-4726-83b7-c7395fdc167b.jpg?w=768",
            "xhdpi": "https://edge.one.fit/image/partner/image/551/2e3ed679-39f1-4726-83b7-c7395fdc167b.jpg?w=720",
            "hdpi": "https://edge.one.fit/image/partner/image/551/2e3ed679-39f1-4726-83b7-c7395fdc167b.jpg?w=540",
            "mdpi": "https://edge.one.fit/image/partner/image/551/2e3ed679-39f1-4726-83b7-c7395fdc167b.jpg?w=480",
            "ldpi": "https://edge.one.fit/image/partner/image/551/2e3ed679-39f1-4726-83b7-c7395fdc167b.jpg?w=320",
            "thumbnail": "https://edge.one.fit/image/partner/image/551/2e3ed679-39f1-4726-83b7-c7395fdc167b.jpg?w=150",
            "orig": "https://edge.one.fit/image/partner/image/551/2e3ed679-39f1-4726-83b7-c7395fdc167b.jpg"
          },
          "surplus": {
            "surplus_allowed": true,
            "price": 1200,
            "formatted_price": "€ 12,00"
          }
        }
      }
    ]
  }
}
 */
)

@Serializable
data class UsageJson(
    val id: Int // FIXME ??
)

@Serializable
data class ReservationsJson(
    val data: List<ReservationJson>
)

@Serializable
data class ReservationJson(
    val id: Int // FIXME ??
)

data class SearchParams(
    val city: String = "AMS",
    val limit: Int = 20,
    val page: Int = 1,
    val start: String = "2023-02-18T00:00:00+00:00",
    val end: String = "2023-02-18T23:59:59.999Z",
)

@Serializable
data class SearchResultsJson(
    val data: List<SearchResultJson>
)

@Serializable
data class SearchResultJson(
    val id: Int,
    val name: String
/*
			"id": 10933666,
			"name": "Massage Intense",
			"slug": "relax-lounge-massage-intense",
			"spots_available": 1,
			"spots_booked": 1,
			"reservation_allowed": true,
			"from": "2023-02-18T15:30:00+01:00",
			"till": "2023-02-18T16:00:00+01:00",
			"reserved": false,
			"waitlist": false,
			"partner": {
				"id": 16391,
				"name": "Relax Lounge",
				"slug": "relax-lounge-amsterdam",
				"waitlist_enabled": true
			},
			"location": {
				"street": "Kerkstraat",
				"house_number": "163",
				"addition": "",
				"zip_code": "1017 GG",
				"city": "Amsterdam",
				"latitude": 52.363437,
				"longitude": 4.8891307
			},
			"attending_friends": [],
			"is_digital": false
 */
)
