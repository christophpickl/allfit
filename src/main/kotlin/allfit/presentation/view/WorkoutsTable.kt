package allfit.presentation.view

import allfit.presentation.PresentationConstants
import allfit.presentation.logic.StaticImage
import allfit.presentation.logic.StaticImageStorage
import allfit.presentation.models.DateRange
import allfit.presentation.models.FullWorkout
import allfit.presentation.models.Rating
import allfit.presentation.renderStars
import allfit.presentation.tornadofx.applyInitSort
import javafx.beans.value.ObservableValue
import javafx.geometry.Pos
import javafx.scene.control.SelectionMode
import javafx.scene.control.TableColumn
import javafx.scene.control.TableView
import javafx.scene.image.Image
import tornadofx.action
import tornadofx.cellFormat
import tornadofx.column
import tornadofx.contextmenu
import tornadofx.fixedWidth
import tornadofx.imageview
import tornadofx.item
import tornadofx.label
import tornadofx.readonlyColumn
import tornadofx.remainingWidth
import tornadofx.selectedItem
import tornadofx.smartResize
import tornadofx.weightedWidth

class WorkoutsTable() : TableView<FullWorkout>() {

    private val dateColumn: TableColumn<FullWorkout, DateRange>

    init {
        smartResize()

        selectionModel.selectionMode = SelectionMode.SINGLE

        imageColumn { it.value.imageProperty() }

        column<FullWorkout, String>("Workout") { it.value.nameProperty() }.remainingWidth().weightedWidth(0.5)

        dateColumn = readonlyColumn("Date", FullWorkout::date).apply {
            fixedWidth(150)
            cellFormat { date ->
                text = date.prettyString
            }
        }

        column<FullWorkout, Boolean>("Reserved") { it.value.isReservedProperty() }.fixedWidth(60)
            .cellFormat { isReserved ->
                if (isReserved) {
                    graphic = imageview(StaticImageStorage.get(StaticImage.Reserved)) {
                        alignment = Pos.CENTER
                    }
                }
            }

        imageColumn { it.value.partner.imageProperty() }

        column<FullWorkout, String>("Partner") { it.value.partner.nameProperty() }.remainingWidth().weightedWidth(0.5)

        column<FullWorkout, Int>("#") { it.value.partner.checkinsProperty() }.fixedWidth(30)

        column<FullWorkout, Rating>("Rating") { it.value.partner.ratingProperty() }.fixedWidth(80)
            .cellFormat { rating ->
                graphic = label(rating.renderStars())
            }

        column<FullWorkout, Boolean>("Favorite") { it.value.partner.isFavoritedProperty() }.fixedWidth(60)
            .cellFormat { isFavorite ->
                graphic =
                    imageview(StaticImageStorage.get(if (isFavorite) StaticImage.FavoriteFull else StaticImage.FavoriteOutline)) {
                        alignment = Pos.CENTER
                    }
            }

        column<FullWorkout, Boolean>("Wishlist") { it.value.partner.isWishlistedProperty() }.fixedWidth(60)
            .cellFormat { isWishlisted ->
                graphic =
                    imageview(StaticImageStorage.get(if (isWishlisted) StaticImage.WishlistFull else StaticImage.WishlistOutline)) {
                        alignment = Pos.CENTER
                    }
            }

        contextmenu {
            item("Hide Partner").action {
                selectedItem?.also {
                    // FIXME: finishing partner hiding feature
                    /*
                    - in DB schema add PARTNER.isHidden: Boolean
                    - update here in PartnerEntity via service-controller
                    - always have default hidden-searchfilter for workout table active
                    - in PartnersView mark hidden ones and make it "unhidden"
                    */
                    println("hiding: ${it.partnerId}")
                }
            }
        }
    }

    // has to be invoked after init
    fun applySort() {
        applyInitSort(dateColumn)
    }

    private fun TableView<FullWorkout>.imageColumn(valueProvider: (TableColumn.CellDataFeatures<FullWorkout, Image>) -> ObservableValue<Image>) {
        column<FullWorkout, Image>("", valueProvider).fixedWidth(PresentationConstants.tableImageWidth + 10)
            .cellFormat {
                graphic = imageview(it) {
                    fitWidth = PresentationConstants.tableImageWidth
                    isPreserveRatio = true
                }
            }
    }
}
