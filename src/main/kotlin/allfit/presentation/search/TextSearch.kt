package allfit.presentation.search

import javafx.scene.control.TextField
import mu.KotlinLogging.logger
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
        title = "Text"
        enabledAction = OnEnabledAction { checkSearch() }
        termsInput = textfield {
            textProperty().addListener { _, _, _ ->
                checkSearch()
            }
        }
    }

    override fun buildSearchRequest() =
        termsInput.text.let { terms ->
            if (terms.isEmpty()) {
                null
            } else {
                TextSearchRequest<T>(
                    terms
                        .trim()
                        .split(" ")
                        .map { it.trim().lowercase() }
                        .filter { it.isNotEmpty() }
                        .also {
                            logger.debug { "Searching for terms: $it" }
                        })
            }
        }
}
