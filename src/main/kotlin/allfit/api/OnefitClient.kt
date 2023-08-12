package allfit.api

import allfit.api.models.CategoriesJsonRoot
import allfit.api.models.CheckinsJsonRoot
import allfit.api.models.PartnersJsonRoot
import allfit.api.models.ReservationsJsonRoot
import allfit.api.models.SingleWorkoutJsonRoot
import allfit.api.models.UsageJsonRoot
import allfit.api.models.WorkoutJson
import allfit.service.endOfDay
import allfit.service.formatIsoOffset
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

interface OnefitClient {

    suspend fun getCategories(): CategoriesJsonRoot
    suspend fun getPartners(params: PartnerSearchParams): PartnersJsonRoot
    suspend fun getWorkouts(params: WorkoutSearchParams): List<WorkoutJson>
    suspend fun getWorkoutById(id: Int): SingleWorkoutJsonRoot
    suspend fun getReservations(): ReservationsJsonRoot
    suspend fun getCheckins(params: CheckinSearchParams): CheckinsJsonRoot
    suspend fun getUsage(): UsageJsonRoot

}

interface PagedParams<THIS : PagedParams<THIS>> {
    val limit: Int
    val page: Int
    fun nextPage(): THIS
}

data class PartnerSearchParams(
    val city: String,
    val pageItemCount: Int,
//    val radiusInMeters: Int,
//    val zipCode: String,
) {
    companion object {
        fun simple() =
            PartnerSearchParams(
                city = "AMS",
                pageItemCount = 5_000,
//                radiusInMeters = 3_000,
//                zipCode = "1011HW",
            )
    }
}

data class CheckinSearchParams(
    override val limit: Int,
    override val page: Int,
) : PagedParams<CheckinSearchParams> {
    override fun nextPage() = copy(page = page + 1)

    companion object {
        fun simple(limit: Int = 500) = CheckinSearchParams(
            page = 1,
            limit = limit,
        )
    }
}

data class WorkoutSearchParams(
    val city: String,
    val start: ZonedDateTime,
    val end: ZonedDateTime,
    val isDigital: Boolean,
    override val limit: Int,
    override val page: Int,
) : PagedParams<WorkoutSearchParams> {

    val startFormatted = start.formatIsoOffset()
    val endFormatted = end.formatIsoOffset()
    val totalDays = ChronoUnit.DAYS.between(start, end).toInt()

    constructor(
        from: ZonedDateTime,
        plusDays: Int,
        limit: Int = 5_000, // must not be 15k (or similar) otherwise throws 500! we follow pagination anyway ;)
    ) : this(
        city = "AMS",
        limit = limit,
        page = 1,
        start = from,
        end = from.plusDays(plusDays.toLong()).endOfDay(),
        isDigital = false,
    )

    override fun nextPage() = copy(page = page + 1)
}
