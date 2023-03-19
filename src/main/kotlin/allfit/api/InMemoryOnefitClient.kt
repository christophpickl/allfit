package allfit.api

import allfit.api.models.CategoriesJson
import allfit.api.models.CheckinsJson
import allfit.api.models.MetaJson
import allfit.api.models.PartnersJson
import allfit.api.models.ReservationsJson
import allfit.api.models.WorkoutsJson

class InMemoryOnefitClient : OnefitClient {
    var categoriesJson = CategoriesJson(emptyList())
    var partnersJson = PartnersJson(emptyList())
    var workoutsJson = WorkoutsJson(emptyList(), MetaJson.empty)
    var reservationsJson = ReservationsJson(emptyList())
    var checkinsJson = CheckinsJson(emptyList(), MetaJson.empty)
    override suspend fun getCategories() = categoriesJson
    override suspend fun getPartners(params: PartnerSearchParams) = partnersJson
    override suspend fun getWorkouts(params: WorkoutSearchParams) = workoutsJson
    override suspend fun getWorkoutById(id: Int) = TODO("fixme me") // FIXME implement me
    override suspend fun getReservations() = reservationsJson
    override suspend fun getCheckins(params: CheckinSearchParams) = checkinsJson
}
