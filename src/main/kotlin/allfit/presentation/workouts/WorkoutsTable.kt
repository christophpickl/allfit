package allfit.presentation.workouts

import allfit.presentation.HidePartnerFXEvent
import allfit.presentation.PresentationConstants
import allfit.presentation.logic.StaticIcon
import allfit.presentation.logic.StaticIconStorage
import allfit.presentation.models.DateRange
import allfit.presentation.models.FullWorkout
import allfit.presentation.tornadofx.applyInitSort
import allfit.presentation.tornadofx.favoriteColumn
import allfit.presentation.tornadofx.imageColumn
import allfit.presentation.tornadofx.ratingColumn
import allfit.presentation.tornadofx.wishlistColumn
import allfit.service.Clock
import javafx.scene.control.SelectionMode
import javafx.scene.control.TableColumn
import javafx.scene.control.TableView
import tornadofx.FX
import tornadofx.action
import tornadofx.cellFormat
import tornadofx.column
import tornadofx.contextmenu
import tornadofx.fixedWidth
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
        favoriteColumn { it.value.partner.isFavoritedProperty() }
        wishlistColumn { it.value.partner.isWishlistedProperty() }

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
