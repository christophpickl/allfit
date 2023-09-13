package allfit.presentation.search

import allfit.AppConstants
import allfit.presentation.models.FullWorkout
import allfit.service.Clock
import allfit.service.formatDayDate
import allfit.service.formatTime
import allfit.service.toDaysUntil
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import javafx.geometry.Pos
import javafx.scene.control.ComboBox
import javafx.scene.layout.Pane
import tornadofx.action
import tornadofx.addChildIfPossible
import tornadofx.asObservable
import tornadofx.button
import tornadofx.combobox
import tornadofx.enableWhen
import tornadofx.hbox
import tornadofx.label
import tornadofx.selectedItem
import tornadofx.singleAssign
import tornadofx.vbox

data class DateSearchRequest(
    val date: ZonedDateTime,
    val timeStart: ZonedDateTime?,
    val timeEnd: ZonedDateTime?,
) : SubSearchRequest<FullWorkout> {
    override val predicate: (FullWorkout) -> Boolean = { workout ->
        val workoutTime = workout.simpleWorkout.date.start.hour
        workout.date.start.dayOfYear == date.dayOfYear &&
                timeStart.maybeCompareTime(isSmaller = false, workoutTime) &&
                timeEnd.maybeCompareTime(isSmaller = true, workoutTime)
    }

    private fun ZonedDateTime?.maybeCompareTime(isSmaller: Boolean, hour: Int): Boolean =
        if (this == null) true else {
            if (isSmaller) {
                hour <= this.hour
            } else {
                hour >= this.hour
            }
        }
}

private sealed interface TimeOrAll {

    fun format(): String
    val time: ZonedDateTime?

    object All : TimeOrAll {
        override fun format() = "ALL"
        override val time: ZonedDateTime? = null
    }

    class Time(override val time: ZonedDateTime) : TimeOrAll {
        override fun format() = time.formatTime()
    }
}

class DateSearchPane(
    private val clock: Clock,
    checkSearch: () -> Unit,
) : SearchPane<FullWorkout>() {

    private val dayStartsAt = 6
    private val dayEndsAt = 22

    private var dateInput: ComboBox<ZonedDateTime> = buildDateCombobox(checkSearch)
    private var timeStartInput: ComboBox<TimeOrAll> by singleAssign()
    private var timeEndInput: ComboBox<TimeOrAll> by singleAssign()

    override var searchFieldPane = searchField {
        title = "Date"
        enabledAction = OnEnabledAction { checkSearch() }

        vbox(spacing = 5.0) {
            hbox(spacing = 2.0) {
                navigationButton(isBack = true)
                addChildIfPossible(dateInput)
                navigationButton(isBack = false)
            }
            hbox(spacing = 2.0, alignment = Pos.CENTER_LEFT) {
                val times = buildTimes()
                label("From:")
                timeStartInput = timeCombobox(times, checkSearch)
                label("Until:")
                timeEndInput = timeCombobox(times, checkSearch)
            }
        }
    }

    private fun Pane.navigationButton(isBack: Boolean) = button(if (isBack) "<" else ">") {
        action {
            if (isBack) {
                dateInput.selectionModel.selectPrevious()
            } else {
                dateInput.selectionModel.selectNext()
            }
        }
        enableWhen {
            dateInput.selectionModel.selectedIndexProperty().map { selectedIndex ->
                if (isBack) {
                    selectedIndex != 0
                } else {
                    selectedIndex != (dateInput.items.size - 1)
                }
            }
        }
    }

    private fun buildDateCombobox(checkSearch: () -> Unit) = ComboBox(buildDays().asObservable()).apply {
        cellFormat {
            text = it.formatDayDate()
        }
        setOnAction {
            checkSearch()
        }
        selectionModel.selectFirst()
    }

    private fun Pane.timeCombobox(times: List<TimeOrAll>, checkSearch: () -> Unit) =
        combobox(values = times) {
            cellFormat {
                text = it.format()
            }
            setOnAction {
                checkSearch()
            }
            selectionModel.selectFirst()
        }

    private fun buildDays() =
        clock.todayBeginOfDay().toDaysUntil(AppConstants.workoutsIntoFuture)

    private fun buildTimes(): List<TimeOrAll> {
        val today = clock.now().truncatedTo(ChronoUnit.DAYS)
        return buildList {
            this += TimeOrAll.All
            this += (dayStartsAt..dayEndsAt).map {
                TimeOrAll.Time(today.withHour(it))
            }
        }
    }

    override fun buildSearchRequest() =
        DateSearchRequest(
            date = dateInput.selectedItem!!,
            timeStart = timeStartInput.selectedItem!!.time,
            timeEnd = timeEndInput.selectedItem!!.time,
        )
}
