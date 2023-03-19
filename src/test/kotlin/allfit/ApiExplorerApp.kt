package allfit

import allfit.api.OnefitHttpClient
import allfit.api.authenticateOneFit
import allfit.service.CredentialsLoader
import io.kotest.common.runBlocking

class ApiExplorerApp(private val client: OnefitHttpClient) {

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            runBlocking {
                ApiExplorerApp(authenticateOneFit(CredentialsLoader.load()) as OnefitHttpClient).playground()
            }
        }
    }

    private suspend fun playground() {
//        println(client.getCheckins(CheckinSearchParams.simple()))
        println(client.getWorkoutById(11033240))
//        println(client.getPartners(PartnerSearchParams.simple()))
//        val workouts = client.getWorkouts(
//            WorkoutSearchParams.simple(
//                from = SystemClock.todayBeginOfDay(),
//                plusDays = 1
//            )
//        )
//        println("found ${workouts.data.size} workouts")
//        println(workouts)
    }
}
