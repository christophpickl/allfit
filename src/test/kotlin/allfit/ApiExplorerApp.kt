package allfit

import allfit.api.OnefitClient
import allfit.api.PartnerSearchParams
import allfit.api.RealOnefitClient
import allfit.api.WorkoutSearchParams
import allfit.service.CredentialsLoader
import allfit.service.SystemClock
import io.kotest.common.runBlocking

class ApiExplorerApp(private val client: RealOnefitClient) {

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            runBlocking {
                ApiExplorerApp(OnefitClient.authenticate(CredentialsLoader.load()) as RealOnefitClient).playground()
            }
        }
    }

    private suspend fun playground() {
        println(client.getPartners(PartnerSearchParams.simple()))
        val workouts = client.getWorkouts(
            WorkoutSearchParams.simple(
                from = SystemClock.todayBeginOfDay(),
                plusDays = 1
            )
        )
        println("found ${workouts.data.size} workouts")
        println(workouts)
    }
}
