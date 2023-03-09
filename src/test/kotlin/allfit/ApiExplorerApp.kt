package allfit

import allfit.api.OnefitClient
import io.kotest.common.runBlocking

object ApiExplorerApp {

    @JvmStatic
    fun main(args: Array<String>) {
        runBlocking {
            playground(
                "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJiS3E3eXRMU0JRSVByV1hNNDA1U3pZZFhXUzFQdWI3SWF0U1BNUy1ZdVZZIn0.eyJleHAiOjE2ODA5NDM4MTEsImlhdCI6MTY3ODM1MTgxMSwianRpIjoiOTE5ZDQ1NzUtZDczZi00NzNmLWJmZTAtNGIyODljNTg4MzVlIiwiaXNzIjoiaHR0cDovL2tleWNsb2FrLWh0dHAub25lZml0LWxpdmUuc3ZjLmNsdXN0ZXIubG9jYWwvYXV0aC9yZWFsbXMvT25lRml0Iiwic3ViIjoiNmY1M2Q2ODMtZDljYS00YzFhLWE3NWYtZTVjZGRiZDlmMzRhIiwidHlwIjoiQmVhcmVyIiwiYXpwIjoid2ViIiwic2Vzc2lvbl9zdGF0ZSI6ImZjYTRjODM4LTgxYTAtNGYwNS05OTRmLTdhNTVkZGU2NTlmNiIsImFjciI6IjEiL…w2Ka3QTRvwbjIb-FN4-qhzWIIDqNPJHR-0uOn3En_2dwMwT5MxjWRas4J0J99HRgpmuXUQNRiiyqKB_2m-1zsRShYcpA1jmWrv7eYLbjTzIFobIixGPg0YjH8letm2lpCE9ldIUcbChVLV4obKi7eAH9Zzm0X0YaKxFBIP3gBoLthWOKQrViF3ZYl8Y57395hcfsheMU4YRAMJrGTIdur-1YPrQDHvPRo7599ROo7LSPfhk0O9On86yyMyHtC4mDQh-8TEVJkqWuAC_CDIJOGc01mfnklqXbPzxel84rccgieMEw-RcE6eVzE9fCf1Shctw2KpVTc0GrnLvph4s1QbShAouCY8eAmAZJ3_FG8ccAcMB7luph--wqMWDyY8dpl1LResEa6fr_eGPAIYxG4jSU_YYxY3L0zAfid4gU2w6XA-NOBQ42u2cRFZJhLS2N-e6axa6mG1GLZiKcJIhN6inzDNHAe9Fy0e8trbOk98I4YBHItkibyFHBQ_ZFPuY"
            )
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
        println()
        println("PARTNERS")
        println(client.getPartnersCategories())
    }
}
