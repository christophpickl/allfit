package allfit.presentation.workouts

import allfit.presentation.logic.StaticIcon
import allfit.presentation.logic.StaticIconStorage
import allfit.presentation.models.SimpleWorkout
import allfit.service.Clock
import io.github.oshai.kotlinlogging.KotlinLogging.logger
import javafx.collections.ObservableList
import javafx.event.EventTarget
import javafx.scene.control.SelectionMode
import javafx.scene.control.TableView
import tornadofx.attachTo
import tornadofx.cellFormat
import tornadofx.column
import tornadofx.fixedWidth
import tornadofx.imageview
import tornadofx.onDoubleClick
import tornadofx.onSelectionChange
import tornadofx.remainingWidth
import tornadofx.rowItem
import tornadofx.selectedItem
import tornadofx.smartResize

fun EventTarget.workoutTable(
    items: ObservableList<SimpleWorkout>,
    onSelected: (SimpleWorkout) -> Unit,
    clock: Clock,
    withTable: WorkoutTable.() -> Unit = {},
) = WorkoutTable(items, onSelected, clock).attachTo(this, withTable)

class WorkoutTable(
    items: ObservableList<SimpleWorkout>,
    onSelected: (SimpleWorkout) -> Unit,
    clock: Clock,
) : TableView<SimpleWorkout>(items) {

    private val logger = logger {}

    init {
        selectionModel.selectionMode = SelectionMode.SINGLE

        onSelectionChange {
            logger.debug { "selection changed to: $selectedItem" }
            selectedItem?.let {
                onSelected(it)
            }
        }
        onDoubleClick {
            logger.debug { "double click for: $selectedItem" }
            selectedItem?.let {
                onSelected(it)
            }
        }

        column("Name", SimpleWorkout::name).apply {
            minWidth = 190.0
            remainingWidth()
            cellFormat { name ->
                graphic = if (rowItem.isReserved) imageview(StaticIconStorage.get(StaticIcon.Reserved)).also { img ->
                    img.fitWidth = 20.0
                    img.fitHeight = 20.0
                    img.isPreserveRatio = true
                } else null
                text = name
            }
        }
        column("Date", SimpleWorkout::date).apply {
            fixedWidth(150)
            cellFormat { date ->
                text = date.toPrettyString(clock)
            }
        }

        smartResize()
    }
}
