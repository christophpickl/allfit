package allfit.presentation.search

import allfit.presentation.components.wishlistToggleButton
import javafx.scene.control.ToggleButton
import tornadofx.singleAssign

interface IsWishlistable {
    val isWishlisted: Boolean
}

data class WishlistedSearchRequest<T : IsWishlistable>(
    val operand: Boolean,
) : SubSearchRequest<T> {
    override val predicate: (T) -> Boolean = {
        it.isWishlisted == operand
    }
}

class WishlistedSearchPane<T : IsWishlistable>(checkSearch: () -> Unit) : SearchPane<T>() {

    private var wishlistedOperand: ToggleButton by singleAssign()

    override var searchFieldPane = searchField {
        title = "Wishlisted"
        enabledAction = OnEnabledAction { checkSearch() }
        wishlistedOperand = wishlistToggleButton {
            isSelected = true
            selectedProperty().addListener { _ ->
                checkSearch()
            }
        }
    }

    override fun buildSearchRequest() = WishlistedSearchRequest<T>(operand = wishlistedOperand.isSelected)
}
