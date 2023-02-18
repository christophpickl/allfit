package allfit

import allfit.api.OnefitClient
import io.kotest.common.runBlocking

object ApiExplorerApp {

    @JvmStatic
    fun main(args: Array<String>) {
        runBlocking {
            playground("XXX")
//            login()
        }
    }

    private suspend fun login() {
        OnefitClient.authenticate("XXX", "XXX")
    }

    private suspend fun playground(authToken: String) {
        val client = OnefitClient(authToken)
//            println(client.getCategories())
//            println(client.search(SearchParams()))
        println(client.getUsage())
    }
}
