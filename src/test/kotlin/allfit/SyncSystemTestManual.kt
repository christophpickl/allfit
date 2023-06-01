package allfit

import allfit.api.authenticateOneFit
import allfit.service.CredentialsLoader
import allfit.service.SystemClock
import allfit.sync.core.Syncer
import io.kotest.common.runBlocking
import org.koin.core.context.startKoin

object SyncSystemTestManual {
    @JvmStatic
    fun main(args: Array<String>) {
        runBlocking {
            println("Start ...")
            println()

            val client = authenticateOneFit(CredentialsLoader.load(), SystemClock)
            val koin = startKoin {
                modules(rootModule(AppConfig.prod, client))
            }.koin
            koin.get<Syncer>().syncAll()

            println()
            println("... Done")
        }
    }
}
