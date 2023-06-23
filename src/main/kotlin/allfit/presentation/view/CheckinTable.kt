package allfit.presentation.view

import allfit.presentation.models.Checkin
import allfit.service.Clock
import allfit.service.toPrettyString
import javafx.collections.ObservableList
import javafx.event.EventTarget
import javafx.scene.control.SelectionMode
import javafx.scene.control.TableView
import tornadofx.attachTo
import tornadofx.column
import tornadofx.fixedWidth
import tornadofx.remainingWidth
import tornadofx.smartResize
import tornadofx.toProperty

fun EventTarget.checkinTable(
    items: ObservableList<Checkin>,
    clock: Clock,
    withTable: CheckinTable.() -> Unit,
) = CheckinTable(items, clock).attachTo(this, withTable)

class CheckinTable(
    items: ObservableList<Checkin>,
    clock: Clock,
) : TableView<Checkin>(items) {
    init {
        selectionModel.selectionMode = SelectionMode.SINGLE

        column<Checkin, String>("Name") {
            when (val checkin = it.value) {
                is Checkin.WorkoutCheckin -> checkin.workout.nameProperty()
                is Checkin.DropinCheckin -> "Drop-In".toProperty()
            }
        }.apply {
            minWidth = 250.0
            remainingWidth()
        }

        column<Checkin, String>("Date") {
            when (val checkin = it.value) {
                is Checkin.WorkoutCheckin -> checkin.workout.date.toPrettyString(clock).toProperty()
                is Checkin.DropinCheckin -> checkin.date.toPrettyString(clock).toProperty()
            }
        }.apply {
            fixedWidth(150)
        }

        smartResize()
    }
}
