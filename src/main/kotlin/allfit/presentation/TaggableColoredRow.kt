package allfit.presentation

import allfit.domain.Taggable
import javafx.scene.control.TableRow
import tornadofx.removeClass
import tornadofx.toggleClass

class TaggableColoredRow<X : Taggable> : TableRow<X>() {
    override fun updateItem(taggable: X?, empty: Boolean) {
        super.updateItem(taggable, empty)
        if (!empty && taggable != null) {
            if (taggable.isWishlisted) {
                toggleClass(Styles.rowWishlist, !isSelected)
                toggleClass(Styles.rowWishlistSelected, isSelected)
                removeClass(Styles.rowFavorite, Styles.rowFavoriteSelected)
            } else if (taggable.isFavorited) {
                toggleClass(Styles.rowFavorite, !isSelected)
                toggleClass(Styles.rowFavoriteSelected, isSelected)
                removeClass(Styles.rowWishlist, Styles.rowWishlistSelected)
            } else {
                removeAllClasses()
            }
        } else {
            removeAllClasses()
        }
    }

    private fun removeAllClasses() {
        removeClass(
            Styles.rowWishlist, Styles.rowWishlistSelected,
            Styles.rowFavorite, Styles.rowFavoriteSelected,
        )
    }
}