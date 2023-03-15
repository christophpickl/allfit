package allfit.api

import allfit.api.models.CategoriesJson
import allfit.api.models.PartnersJson
import allfit.api.models.ReservationsJson
import allfit.api.models.WorkoutsJson
import allfit.service.readApiResponse

object ClassPathOnefitClient : OnefitClient {
    override suspend fun getCategories() =
        readApiResponse<CategoriesJson>("partners_categories.json")

    override suspend fun getPartners(params: PartnerSearchParams) =
        readApiResponse<PartnersJson>("partners_search.json")

    override suspend fun getWorkouts(params: WorkoutSearchParams) =
        readApiResponse<WorkoutsJson>("workouts_search.json")

    override suspend fun getReservations() =
        readApiResponse<ReservationsJson>("reservations.json")
}