package allfit

import allfit.api.OnefitHttpClient
import allfit.api.WorkoutSearchParams
import allfit.api.authenticateOneFit
import allfit.service.CredentialsLoader
import allfit.service.SystemClock
import io.kotest.common.runBlocking

class ApiExplorerApp(private val client: OnefitHttpClient) {

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            runBlocking {
                ApiExplorerApp(
                    authenticateOneFit(
                        CredentialsLoader.load(),
                        SystemClock
                    ) as OnefitHttpClient
                ).playground()
            }
        }
    }

    private suspend fun playground() {
//        println(client.getCheckins(CheckinSearchParams.simple()))
//        println(client.getWorkoutById(11033240))
//        println(client.getPartners(PartnerSearchParams.simple()))
        val workouts = client.getWorkouts(
            WorkoutSearchParams(
                from = SystemClock.todayBeginOfDay(),//.plusDays(7),
                plusDays = 14
            )
        )
        println("found ${workouts.size} workouts")
//        println(workouts)
    }
}
