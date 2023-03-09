package allfit

import allfit.api.RealOnefitClient
import io.kotest.common.runBlocking

object ApiExplorerApp {

    @JvmStatic
    fun main(args: Array<String>) {
        runBlocking {
            playground("")
        }
    }

    private suspend fun playground(authToken: String) {
        val client = RealOnefitClient(authToken)
//            println(client.getCategories())
//            println(client.search(SearchParams()))
        println()
        println("PARTNERS")
        println(client.getPartnersCategories())
    }
}
