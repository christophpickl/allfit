package allfit.presentation.search

import allfit.presentation.models.FullWorkout
import allfit.service.Clock
import allfit.service.formatDayDate
import allfit.service.formatTime
import allfit.service.toDaysUntil
import javafx.scene.control.ComboBox
import tornadofx.*
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

data class DateSearchRequest(
    val date: ZonedDateTime,
    val time: ZonedDateTime,
) : SubSearchRequest {
    override val predicate: (FullWorkout) -> Boolean = { workout ->
        workout.simpleWorkout.date.start.dayOfYear == date.dayOfYear &&
                workout.simpleWorkout.date.start.hour >= time.hour
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
    private var timeInput: ComboBox<ZonedDateTime> by singleAssign()

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
                text = it.formatTime()
            }
            setOnAction {
                checkSearch()
            }
            val timeHour = clock.now().hour
            if (timeHour in dayStartsAt..dayEndsAt) {
                selectionModel.select(timeHour - dayStartsAt)
            }
        }
    }

    private fun buildDays() = clock.todayBeginOfDay().toDaysUntil(searchIntoFutureInDays)

    private fun buildTimes(): List<ZonedDateTime> {
        val today = clock.now().truncatedTo(ChronoUnit.DAYS)
        return (dayStartsAt..dayEndsAt).map {
            today.withHour(it)
        }
    }

    override fun buildSearchRequest() =
        DateSearchRequest(
            date = dateInput.selectedItem!!,
            time = timeInput.selectedItem!!,
        )
}
