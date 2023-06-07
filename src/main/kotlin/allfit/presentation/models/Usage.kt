package allfit.presentation.models

import java.time.ZonedDateTime
import javafx.beans.property.SimpleObjectProperty
import tornadofx.ViewModel

data class Usage(
    val total: Int,
    val noShows: Int,
    val from: ZonedDateTime,
    val until: ZonedDateTime,
    val periodCap: Int, // 16
    val maxCheckInsOrReservationsPerPeriod: Int, // 4
    val totalCheckInsOrReservationsPerDay: Int, // 2
    val maxReservations: Int, // 6
)

class UsageModel : ViewModel() {
    val usage = SimpleObjectProperty<Usage>()
    val today = SimpleObjectProperty<ZonedDateTime>()
}
