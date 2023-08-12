package allfit.api

import allfit.api.models.CategoriesJsonRoot
import allfit.api.models.CheckinsJsonRoot
import allfit.api.models.MetaJson
import allfit.api.models.PartnersJsonRoot
import allfit.api.models.ReservationsJsonRoot
import allfit.api.models.SingleWorkoutJsonRoot
import allfit.api.models.UsageJson
import allfit.api.models.UsageJsonRoot
import allfit.api.models.UsagePeriodJson
import allfit.api.models.UsageProductJson
import allfit.api.models.WorkoutsJsonRoot
import java.time.ZonedDateTime

class InMemoryOnefitClient : OnefitClient {

    var categoriesJson = CategoriesJsonRoot(emptyList())
    var partnersJson = PartnersJsonRoot(emptyList())
    var workoutsJson = WorkoutsJsonRoot(emptyList(), MetaJson.empty)
    var workoutsById = mutableMapOf<Int, SingleWorkoutJsonRoot>()
    var reservationsJson = ReservationsJsonRoot(emptyList())
    var checkinsJson = CheckinsJsonRoot(emptyList(), MetaJson.empty)
    var usageJson = UsageJsonRoot(
        UsageJson(
            total = 0,
            no_shows = 0,
            period = UsagePeriodJson(
                display_from = ZonedDateTime.now(),
                display_till = ZonedDateTime.now().plusDays(1),
                product = UsageProductJson(
                    rules = emptyList()
                )
            )
        )
    )

    override suspend fun getCategories() = categoriesJson
    override suspend fun getPartners(params: PartnerSearchParams) = partnersJson
    override suspend fun getWorkouts(params: WorkoutSearchParams) = workoutsJson.data
    override suspend fun getWorkoutById(id: Int) = workoutsById[id]!!
    override suspend fun getReservations() = reservationsJson
    override suspend fun getCheckins(params: CheckinSearchParams) = checkinsJson
    override suspend fun getUsage() = usageJson

}
