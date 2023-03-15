package allfit.api

import allfit.api.models.CategoriesJson
import allfit.api.models.MetaJson
import allfit.api.models.MetaPaginationJson
import allfit.api.models.PartnersJson
import allfit.api.models.ReservationsJson
import allfit.api.models.WorkoutsJson

class InMemoryOnefitClient : OnefitClient {
    var categoriesJson = CategoriesJson(emptyList())
    var partnersJson = PartnersJson(emptyList())
    var workoutsJson = WorkoutsJson(emptyList(), MetaJson(MetaPaginationJson(1, 1)))
    var reservationsJson = ReservationsJson(emptyList())
    override suspend fun getCategories() = categoriesJson
    override suspend fun getPartners(params: PartnerSearchParams) = partnersJson
    override suspend fun getWorkouts(params: WorkoutSearchParams) = workoutsJson
    override suspend fun getReservations() = reservationsJson
}