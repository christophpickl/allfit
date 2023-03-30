package allfit.presentation

import allfit.presentation.models.Rating
import allfit.presentation.models.SimplePartner
import javafx.beans.value.ObservableValue
import javafx.event.EventTarget
import javafx.scene.web.WebView
import tornadofx.webview

object PresentationConstants {
    const val tableImageWidth = 50.0
}

private val starsMap = mapOf(
    0 to "",
    1 to "⭐️",
    2 to "⭐️⭐️",
    3 to "⭐️⭐️⭐️",
    4 to "⭐️⭐️⭐️⭐️",
    5 to "⭐️⭐️⭐️⭐️⭐️",
)

fun Rating.renderStars() = starsMap[this] ?: error("Invalid rating star entry: $this")

data class PartnerModifications(
    val partnerId: Int,
    val rating: Rating,
    val note: String,
    val isFavorited: Boolean,
    val isWishlisted: Boolean,
) {
    fun update(storedPartner: SimplePartner) {
        storedPartner.rating = rating
        storedPartner.isFavorited = isFavorited
        storedPartner.isWishlisted = isWishlisted
        storedPartner.note = note
    }
}

fun EventTarget.htmlview(html: ObservableValue<String>, op: WebView.() -> Unit = {}) {
    webview {
        html.addListener { _, _, newValue ->
            engine.loadContent(newValue ?: "")
        }
        op()
    }
}
