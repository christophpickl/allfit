@file:UseSerializers(ZonedDateTimeSerializer::class)
@file:Suppress("PropertyName")

package allfit.api.models

import java.time.ZonedDateTime
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

@Serializable
data class UsageJsonRoot(
    val data: UsageJson,
)

@Serializable
data class UsageJson(
    val total: Int,
    val no_shows: Int,
    val period: UsagePeriodJson,
)

@Serializable
data class UsagePeriodJson(
    val display_from: ZonedDateTime,
    val display_till: ZonedDateTime,
    val product: UsageProductJson,
)

@Serializable
data class UsageProductJson(
    val rules: List<UsageProductRuleJson>,
)

@Serializable
data class UsageProductRuleJson(
    val type: String,
    val unit: String?,
    val amount: Int,
) {
    object Types {
        const val PERIOD_CAP = "GeneralPeriodCap" // 16
        const val MAX_PER_PERIOD = "MaxCheckInsOrReservationsPerPeriod" // 4
        const val TOTAL_PER_DAY = "TotalCheckInsOrReservationsPerDay" // 2
        const val MAX_RESERVATIONS = "MaxReservations" // 6
    }
}
