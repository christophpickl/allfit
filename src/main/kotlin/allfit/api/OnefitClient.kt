package allfit.api

import allfit.api.models.CategoriesJson
import allfit.api.models.CheckinsJson
import allfit.api.models.PartnersJson
import allfit.api.models.ReservationsJson
import allfit.api.models.SingleWorkoutJson
import allfit.api.models.WorkoutsJson
import allfit.service.formatOnefit
import java.time.ZonedDateTime

interface OnefitClient {

    suspend fun getCategories(): CategoriesJson
    suspend fun getPartners(params: PartnerSearchParams): PartnersJson
    suspend fun getWorkouts(params: WorkoutSearchParams): WorkoutsJson
    suspend fun getWorkoutById(id: Int): SingleWorkoutJson
    suspend fun getReservations(): ReservationsJson
    suspend fun getCheckins(params: CheckinSearchParams): CheckinsJson

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
    override val limit: Int,
    override val page: Int,
    val start: ZonedDateTime,
    val end: ZonedDateTime,
    val isDigital: Boolean
) : PagedParams<WorkoutSearchParams> {

    val startFormatted = start.formatOnefit()
    val endFormatted = end.formatOnefit()

    override fun nextPage() = copy(page = page + 1)

    companion object {
        fun simple(
            from: ZonedDateTime,
            plusDays: Int,
            limit: Int = 5_000, // must not be 15k (or similar) otherwise throws 500! we follow pagination anyway ;)
        ): WorkoutSearchParams {
            return WorkoutSearchParams(
                city = "AMS",
                limit = limit,
                page = 1,
                start = from,
                end = from.plusDays(plusDays.toLong()),
                isDigital = false,
            )
        }
    }
}
