package allfit.presentation.view

import allfit.presentation.SearchFXEvent
import tornadofx.View
import tornadofx.action
import tornadofx.button
import tornadofx.hbox
import tornadofx.plusAssign
import tornadofx.textfield

class SearchView : View() {

    private val searchTextfield = textfield()

    override val root = hbox {
        this += searchTextfield
        button("Search") {
            action {
                fire(SearchFXEvent)
            }
        }
    }
}