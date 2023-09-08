package allfit.presentation.logic

import java.net.URLEncoder

private const val MAPS_BASE_URL = "https://www.google.com/maps/search/"

fun googleMapsSearchUrl(address: String): String =
    "$MAPS_BASE_URL?api=1&query=${URLEncoder.encode(address, "UTF-8")}"
