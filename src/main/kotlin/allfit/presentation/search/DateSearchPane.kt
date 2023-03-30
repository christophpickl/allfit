package allfit.presentation.search

import allfit.presentation.models.FullWorkout
import allfit.service.toZonedDate
import javafx.scene.control.DatePicker
import tornadofx.datepicker
import tornadofx.singleAssign
import java.time.ZonedDateTime

data class DateSearchRequest(
    val operand: ZonedDateTime,
) : SubSearchRequest {
    override val predicate: (FullWorkout) -> Boolean = { workout ->
        workout.simpleWorkout.date.start.dayOfYear == operand.dayOfYear
    }
}

class DateSearchPane(checkSearch: () -> Unit) : SearchPane() {
    override var searchFieldPane: SearchFieldPane by singleAssign()
    var dateInput: DatePicker by singleAssign()

    init {
        searchFieldPane = searchField {
            title = "Date"
            enabledAction = OnEnabledAction { checkSearch() }
            dateInput = datepicker {
                prefWidth = 120.0
                isShowWeekNumbers = false
                setOnAction {
                    checkSearch()
                }
            }
        }
    }

    override fun buildSearchRequest() = dateInput.value?.let {
        DateSearchRequest(operand = it.toZonedDate())
    }
}
