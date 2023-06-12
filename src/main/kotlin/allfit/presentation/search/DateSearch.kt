package allfit.presentation.search

import allfit.presentation.models.FullWorkout
import allfit.service.Clock
import allfit.service.formatDayDate
import allfit.service.formatTime
import allfit.service.toDaysUntil
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import javafx.scene.control.ComboBox
import tornadofx.action
import tornadofx.button
import tornadofx.combobox
import tornadofx.selectedItem
import tornadofx.singleAssign

data class DateSearchRequest(
    val date: ZonedDateTime,
    val time: ZonedDateTime?,
) : SubSearchRequest {
    override val predicate: (FullWorkout) -> Boolean = { workout ->
        workout.simpleWorkout.date.start.dayOfYear == date.dayOfYear &&
                (if (time == null) true else {
                    workout.simpleWorkout.date.start.hour >= time.hour
                })
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
) : SearchPane() {

    private val searchIntoFutureInDays = 14
    private val dayStartsAt = 6
    private val dayEndsAt = 22

    private var dateInput: ComboBox<ZonedDateTime> by singleAssign()
    private var timeInput: ComboBox<TimeOrAll> by singleAssign()

    override var searchFieldPane = searchField {
        title = "Date"
        enabledAction = OnEnabledAction { checkSearch() }
        button("-") {
            action {
                dateInput.selectionModel.selectPrevious()
            }
        }
        dateInput = combobox(values = buildDays()) {
            cellFormat {
                text = it.formatDayDate()
            }
            setOnAction {
                checkSearch()
            }
            selectionModel.selectFirst()
        }
        button("+") {
            action {
                dateInput.selectionModel.selectNext()
            }
        }
        timeInput = combobox(values = buildTimes()) {
            cellFormat {
                text = it.format()
            }
            setOnAction {
                checkSearch()
            }
            selectionModel.selectFirst()
        }
    }

    private fun buildDays() =
        clock.todayBeginOfDay().toDaysUntil(searchIntoFutureInDays)

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
            time = timeInput.selectedItem!!.time,
        )
}
