package allfit.presentation.view

import allfit.presentation.models.Checkin
import allfit.presentation.models.SimpleWorkout
import allfit.service.Clock
import allfit.service.toPrettyString
import io.github.oshai.kotlinlogging.KotlinLogging.logger
import javafx.collections.ObservableList
import javafx.event.EventTarget
import javafx.scene.control.SelectionMode
import javafx.scene.control.TableView
import tornadofx.attachTo
import tornadofx.column
import tornadofx.fixedWidth
import tornadofx.onDoubleClick
import tornadofx.onSelectionChange
import tornadofx.remainingWidth
import tornadofx.selectedItem
import tornadofx.smartResize
import tornadofx.toProperty

fun EventTarget.checkinTable(
    items: ObservableList<Checkin>,
    onSelected: (SimpleWorkout) -> Unit,
    clock: Clock,
    withTable: CheckinTable.() -> Unit = {},
) = CheckinTable(items, onSelected, clock).attachTo(this, withTable)

class CheckinTable(
    items: ObservableList<Checkin>,
    private val onSelected: (SimpleWorkout) -> Unit,
    clock: Clock,
) : TableView<Checkin>(items) {

    private val logger = logger {}

    init {
        selectionModel.selectionMode = SelectionMode.SINGLE

        onSelectionChange {
            logger.debug { "selection changed to: $selectedItem" }
            selectedItem?.let {
                onCheckinSelected(it)
            }
        }
        onDoubleClick {
            logger.debug { "double click for: $selectedItem" }
            selectedItem?.let {
                onCheckinSelected(it)
            }
        }

        column<Checkin, String>("Name") {
            when (val checkin = it.value) {
                is Checkin.WorkoutCheckin -> checkin.workout.nameProperty()
                is Checkin.DropinCheckin -> "Drop-In".toProperty()
            }
        }.apply {
            minWidth = 190.0
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

    private fun onCheckinSelected(checkin: Checkin) {
        when (checkin) {
            is Checkin.WorkoutCheckin -> onSelected(checkin.workout)
            is Checkin.DropinCheckin -> logger.debug { "No workout selection shown for dropin checkins." }
        }
    }
}
