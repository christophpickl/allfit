package allfit.presentation

import allfit.presentation.models.Rating
import javafx.beans.property.ObjectProperty
import javafx.beans.value.ObservableValue
import javafx.collections.ObservableList
import javafx.event.EventTarget
import javafx.scene.web.WebView
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

fun EventTarget.htmlview(html: ObservableValue<String>, op: WebView.() -> Unit = {}) =
    webview {
        engine.userStyleSheetLocation = "data:,body { font: 12px Arial; }";
        html.addListener { _, _, newValue ->
            engine.loadContent(newValue ?: "")
        }
        op()
    }

fun ObjectProperty<Boolean>.addListenerAndInit(function: (Boolean) -> Unit) {
    function(get())
    addListener { _, _, newValue ->
        function(newValue)
    }
}

fun <E : Enum<E>> ObservableList<E>.addAtSortPosition(enum: E) {
    add(calcIconIndex(iterator(), enum), enum)
}

private fun <E : Enum<E>> calcIconIndex(otherEnums: Iterator<E>, enum: E): Int {
    var index = 0
    while (otherEnums.hasNext()) {
        val other = otherEnums.next()
        if (enum.ordinal > other.ordinal) {
            index++
        } else {
            break
        }
    }
    return index
}

fun <E : Enum<E>> ObservableList<E>.manageEnum(property: ObjectProperty<Boolean>, icon: E) {
    property.addListenerAndInit { value ->
        if (value) addAtSortPosition(icon) else remove(icon)
    }
}
