package allfit.api

import allfit.api.models.CategoriesJsonRoot
import allfit.api.models.CheckinsJsonRoot
import allfit.api.models.PartnersJsonRoot
import allfit.api.models.ReservationsJsonRoot
import allfit.api.models.SingleWorkoutJsonRoot
import allfit.api.models.UsageJsonRoot
import allfit.api.models.WorkoutsJsonRoot
import allfit.service.readApiResponse

object ClassPathOnefitClient : OnefitClient {
    override suspend fun getCategories() =
        readApiResponse<CategoriesJsonRoot>("partners_categories.json")

    override suspend fun getPartners(params: PartnerSearchParams) =
        readApiResponse<PartnersJsonRoot>("partners_search.json")

    override suspend fun getWorkouts(params: WorkoutSearchParams) =
        readApiResponse<WorkoutsJsonRoot>("workouts_search.json").data

    override suspend fun getWorkoutById(id: Int) =
        readApiResponse<SingleWorkoutJsonRoot>("workout_single.json")

    override suspend fun getReservations() =
        readApiResponse<ReservationsJsonRoot>("reservations.json")

    override suspend fun getCheckins(params: CheckinSearchParams) =
        readApiResponse<CheckinsJsonRoot>("checkins.json").data

    override suspend fun getUsage() =
        readApiResponse<UsageJsonRoot>("usage.json")
}
