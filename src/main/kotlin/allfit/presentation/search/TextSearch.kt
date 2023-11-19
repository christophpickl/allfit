package allfit.presentation.search

import io.github.oshai.kotlinlogging.KotlinLogging.logger
import javafx.scene.control.TextField
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import tornadofx.singleAssign
import tornadofx.textfield

interface HasTextSearchable {
    val searchableTerms: List<String>
}

data class TextSearchRequest<T : HasTextSearchable>(
    /** already all lower-cased */
    val searchTerms: List<String>,
) : SubSearchRequest<T> {
    override val predicate: (T) -> Boolean = { entity ->
        searchTerms.all { searchTerm ->
            entity.searchableTerms.any { entityTerm ->
                entityTerm.lowercase().contains(searchTerm)
            }
        }
    }
}

class TextSearchPane<T : HasTextSearchable>(checkSearch: () -> Unit) : SearchPane<T>() {

    private val logger = logger {}
    private var termsInput: TextField by singleAssign()

    override var searchFieldPane = searchField {
//        background = Background.fill(Color.BLUE)
        title = "Text"
        enabledAction = OnEnabledAction { checkSearch() }
        termsInput = textfield {
            textProperty().addListener { _, _, _ ->
                checkSearch()
            }
        }
        HBox.setHgrow(termsInput, Priority.ALWAYS)
    }

    override fun buildSearchRequest() = termsInput.text.let { terms ->
        if (terms.isEmpty()) {
            null
        } else {
            TextSearchRequest<T>(terms.trim().split(" ").map { it.trim().lowercase() }.filter { it.isNotEmpty() }.also {
                logger.debug { "Searching for terms: $it" }
            })
        }
    }
}
