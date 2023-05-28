package allfit.presentation.search

import allfit.presentation.models.FullWorkout
import javafx.scene.control.TextField
import mu.KotlinLogging.logger
import tornadofx.singleAssign
import tornadofx.textfield

data class TextSearchRequest(
    /** already all lower-cased */
    val terms: List<String>,
) : SubSearchRequest {
    override val predicate: (FullWorkout) -> Boolean = { workout ->
        terms.all { term ->
            workout.name.lowercase().contains(term) ||
                    workout.partner.name.lowercase().contains(term)
        }
    }
}

class TextSearchPane(checkSearch: () -> Unit) : SearchPane() {

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
                TextSearchRequest(
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
