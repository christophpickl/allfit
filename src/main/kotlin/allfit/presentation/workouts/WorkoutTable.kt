package allfit.presentation.workouts

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
import tornadofx.onDoubleClick
import tornadofx.onSelectionChange
import tornadofx.remainingWidth
import tornadofx.selectedItem
import tornadofx.smartResize

fun EventTarget.workoutTable(
    items: ObservableList<SimpleWorkout>,
    onSelected: (SimpleWorkout) -> Unit,
    clock: Clock,
    withTable: WorkoutTable.() -> Unit,
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

        column<SimpleWorkout, String>("Name") {
            it.value.nameProperty()
        }.apply {
            minWidth = 250.0
            remainingWidth()
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
