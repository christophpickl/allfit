package allfit.presentation.logic

import java.net.URLEncoder
import javafx.beans.value.ObservableValue

private const val MAPS_BASE_URL = "https://www.google.com/maps/search/"

fun googleMapsSearchUrl(address: ObservableValue<String>): ObservableValue<String> =
    address.map {
        "$MAPS_BASE_URL?api=1&query=${URLEncoder.encode(address.value, "UTF-8")}"
    }
