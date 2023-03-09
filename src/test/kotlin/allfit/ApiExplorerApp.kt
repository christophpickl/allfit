package allfit

import allfit.api.OnefitClient
import allfit.service.CredentialsLoader
import io.kotest.common.runBlocking

class ApiExplorerApp(private val client: OnefitClient) {

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            runBlocking {
                ApiExplorerApp(OnefitClient.authenticate(CredentialsLoader.load())).playground()
            }
        }
    }

    private suspend fun playground() {
        println("PARTNERS")
        println(client.getPartners())
    }
}
