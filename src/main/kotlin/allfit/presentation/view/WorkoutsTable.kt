package allfit.presentation.view

import allfit.presentation.StaticImage
import allfit.presentation.StaticImageStorage
import allfit.presentation.models.FullWorkout
import allfit.presentation.models.Rating
import allfit.presentation.renderStars
import javafx.collections.ObservableList
import javafx.scene.control.SelectionMode
import javafx.scene.control.TableView
import tornadofx.cellFormat
import tornadofx.column
import tornadofx.imageview
import tornadofx.label
import tornadofx.readonlyColumn

class WorkoutsTable(workouts: ObservableList<FullWorkout>) : TableView<FullWorkout>(workouts) {
    init {
        selectionModel.selectionMode = SelectionMode.SINGLE

        readonlyColumn("Name", FullWorkout::name)
        column<FullWorkout, String>("Partner") { it.value.partner.nameProperty() }
        column<FullWorkout, Int>("Visits") { it.value.partner.visitsProperty() }
        column<FullWorkout, Rating>("Rating") { it.value.partner.ratingProperty() }.cellFormat { rating ->
            graphic = label(rating.renderStars())
        }
        column<FullWorkout, Boolean>("Favourite") { it.value.partner.isFavouritedProperty() }.cellFormat { isFavourite ->
            graphic = imageview(StaticImageStorage.get(if (isFavourite) StaticImage.FavouriteFull else StaticImage.FavouriteOutline))
        }
        column<FullWorkout, Boolean>("Wishlist") { it.value.partner.isWishlistedProperty() }.cellFormat { isWishlisted ->
            graphic = imageview(StaticImageStorage.get(if (isWishlisted) StaticImage.WishlistFull else StaticImage.WishlistOutline))
        }
    }
}
