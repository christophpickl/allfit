package allfit.presentation

import allfit.persistence.domain.PartnerEntity
import allfit.persistence.domain.PartnersTable
import allfit.presentation.models.PartnerCustomAttributesWrite
import allfit.presentation.models.Rating
import javafx.beans.value.ObservableValue
import javafx.event.EventTarget
import javafx.scene.web.WebView
import org.jetbrains.exposed.sql.statements.UpdateStatement
import tornadofx.webview

object PresentationConstants {
    const val tableImageWidth = 50.0
    const val downloadImageWidth = 300
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
    fun update(partner: PartnerCustomAttributesWrite) {
        partner.rating = rating
        partner.note = note
        partner.isFavorited = isFavorited
        partner.isWishlisted = isWishlisted
    }

    fun prepare(new: PartnerEntity, stmt: UpdateStatement) {
        stmt[PartnersTable.rating] = new.rating
        stmt[PartnersTable.note] = new.note
        stmt[PartnersTable.isFavorited] = new.isFavorited
        stmt[PartnersTable.isWishlisted] = new.isWishlisted
    }

    fun modify(old: PartnerEntity): PartnerEntity =
        old.copy(
            rating = rating,
            note = note,
            isFavorited = isFavorited,
            isWishlisted = isWishlisted,
        )
}

fun EventTarget.htmlview(html: ObservableValue<String>, op: WebView.() -> Unit = {}) {
    webview {
        html.addListener { _, _, newValue ->
            engine.loadContent(newValue ?: "")
        }
        op()
    }
}
