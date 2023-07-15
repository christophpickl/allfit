package allfit.presentation.view

import allfit.presentation.HidePartnerFXEvent
import allfit.presentation.PresentationConstants
import allfit.presentation.logic.StaticIcon
import allfit.presentation.logic.StaticIconStorage
import allfit.presentation.models.DateRange
import allfit.presentation.models.FullWorkout
import allfit.presentation.tornadofx.applyInitSort
import allfit.presentation.tornadofx.imageColumn
import allfit.presentation.tornadofx.ratingColumn
import allfit.service.Clock
import javafx.geometry.Pos
import javafx.scene.control.SelectionMode
import javafx.scene.control.TableColumn
import javafx.scene.control.TableView
import tornadofx.FX
import tornadofx.action
import tornadofx.cellFormat
import tornadofx.column
import tornadofx.contextmenu
import tornadofx.fixedWidth
import tornadofx.imageview
import tornadofx.item
import tornadofx.readonlyColumn
import tornadofx.remainingWidth
import tornadofx.selectedItem
import tornadofx.smartResize
import tornadofx.weightedWidth

class WorkoutsTable(
    clock: Clock
) : TableView<FullWorkout>() {

    private val dateColumn: TableColumn<FullWorkout, DateRange>
    private val reservedImage = StaticIconStorage.get(StaticIcon.Reserved)
    private val visitedImage = StaticIconStorage.get(StaticIcon.Visited)

    init {
        smartResize()

        selectionModel.selectionMode = SelectionMode.SINGLE

        imageColumn(maxWidth = PresentationConstants.tableImageWidth) { it.value.imageProperty() }

        column<FullWorkout, String>("Workout") { it.value.nameProperty() }.remainingWidth().weightedWidth(0.5)

        dateColumn = readonlyColumn("Date", FullWorkout::date).apply {
            fixedWidth(150)
            cellFormat { date ->
                text = date.toPrettyString(clock)
            }
        }
        column<FullWorkout, String>("Teacher") { it.value.teacherProperty() }.fixedWidth(100)

        imageColumn("Reserved", reservedImage) { it.value.isReservedProperty() }
        imageColumn("Visited", visitedImage) { it.value.wasVisitedProperty() }

        imageColumn(maxWidth = PresentationConstants.tableImageWidth) { it.value.partner.imageProperty() }

        column<FullWorkout, String>("Partner") { it.value.partner.nameProperty() }.remainingWidth().weightedWidth(0.5)

        column<FullWorkout, Int>("Chk") { it.value.partner.checkinsProperty() }.fixedWidth(40)

        ratingColumn { it.value.partner.ratingProperty() }

        column<FullWorkout, Boolean>("Favorite") { it.value.partner.isFavoritedProperty() }.fixedWidth(60)
            .cellFormat { isFavorite ->
                graphic =
                    imageview(StaticIconStorage.get(if (isFavorite) StaticIcon.FavoriteFull else StaticIcon.FavoriteOutline)) {
                        alignment = Pos.CENTER
                    }
            }

        column<FullWorkout, Boolean>("Wishlist") { it.value.partner.isWishlistedProperty() }.fixedWidth(60)
            .cellFormat { isWishlisted ->
                graphic =
                    imageview(StaticIconStorage.get(if (isWishlisted) StaticIcon.WishlistFull else StaticIcon.WishlistOutline)) {
                        alignment = Pos.CENTER
                    }
            }

        contextmenu {
            item("Hide Partner").action {
                selectedItem?.also {
                    FX.eventbus.fire(HidePartnerFXEvent(it.partnerId))
                }
            }
        }
    }

    // has to be invoked after init
    fun applySort() {
        applyInitSort(dateColumn)
    }
}
