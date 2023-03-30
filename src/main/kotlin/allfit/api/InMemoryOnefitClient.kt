package allfit.api

import allfit.api.models.CategoriesJsonRoot
import allfit.api.models.CheckinsJsonRoot
import allfit.api.models.MetaJson
import allfit.api.models.PartnersJsonRoot
import allfit.api.models.ReservationsJsonRoot
import allfit.api.models.SingleWorkoutJsonRoot
import allfit.api.models.WorkoutsJsonRoot

class InMemoryOnefitClient : OnefitClient {
    var categoriesJson = CategoriesJsonRoot(emptyList())
    var partnersJson = PartnersJsonRoot(emptyList())
    var workoutsJson = WorkoutsJsonRoot(emptyList(), MetaJson.empty)
    var workoutsById = mutableMapOf<Int, SingleWorkoutJsonRoot>()
    var reservationsJson = ReservationsJsonRoot(emptyList())
    var checkinsJson = CheckinsJsonRoot(emptyList(), MetaJson.empty)

    override suspend fun getCategories() = categoriesJson
    override suspend fun getPartners(params: PartnerSearchParams) = partnersJson
    override suspend fun getWorkouts(params: WorkoutSearchParams) = workoutsJson
    override suspend fun getWorkoutById(id: Int) = workoutsById[id]!!
    override suspend fun getReservations() = reservationsJson
    override suspend fun getCheckins(params: CheckinSearchParams) = checkinsJson
}
