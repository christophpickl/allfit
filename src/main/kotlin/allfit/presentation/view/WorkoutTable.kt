package allfit.presentation.view

import allfit.presentation.PartnerWorkoutSelectedFXEvent
import allfit.presentation.models.SimpleWorkout
import allfit.service.Clock
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
import tornadofx.readonlyColumn
import tornadofx.remainingWidth
import tornadofx.selectedItem
import tornadofx.smartResize

fun EventTarget.workoutTable(
    items: ObservableList<SimpleWorkout>,
    onSelected: (PartnerWorkoutSelectedFXEvent) -> Unit,
    clock: Clock,
    withTable: WorkoutTable.() -> Unit,
) = WorkoutTable(items, onSelected, clock).attachTo(this, withTable)

class WorkoutTable(
    items: ObservableList<SimpleWorkout>,
    onSelected: (PartnerWorkoutSelectedFXEvent) -> Unit,
    clock: Clock,
) : TableView<SimpleWorkout>(items) {
    init {
        selectionModel.selectionMode = SelectionMode.SINGLE

        onSelectionChange {
            selectedItem?.let {
                onSelected(PartnerWorkoutSelectedFXEvent(it))
            }
        }
        onDoubleClick {
            selectedItem?.let {
                onSelected(PartnerWorkoutSelectedFXEvent(it))
            }
        }

        column<SimpleWorkout, String>("Name") {
            it.value.nameProperty()
        }.apply {
            minWidth = 250.0
            remainingWidth()
        }

        readonlyColumn("Date", SimpleWorkout::date).apply {
            fixedWidth(150)
            cellFormat { date ->
                text = date.toPrettyString(clock)
            }
        }

        smartResize()
    }
}
