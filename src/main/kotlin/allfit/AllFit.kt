package allfit

import allfit.api.OnefitClient
import allfit.service.CredentialsLoader
import allfit.sync.Syncer
import allfit.view.AllFitApp
import mu.KotlinLogging.logger
import org.koin.core.Koin
import org.koin.core.component.KoinComponent
import org.koin.core.context.startKoin
import tornadofx.DIContainer
import tornadofx.FX
import tornadofx.launch
import kotlin.reflect.KClass

object AllFit {
    private val log = logger {}

    @JvmStatic
    suspend fun main(args: Array<String>) {
        log.info { "Starting up AllFit." }
        startupKoin().get<AllFitStarter>().start()
    }

    private suspend fun startupKoin(): Koin {
        val client = buildClient()
        return startKoin {
            log.debug { "Starting up Koin context." }
            modules(mainModule(client))
        }.koin
    }

    private suspend fun buildClient(): OnefitClient =
        OnefitClient.authenticate(CredentialsLoader.load())
}

class AllFitStarter(private val syncer: Syncer) {

    private val log = logger {}

    suspend fun start() {
        syncer.sync()
        startTornadoFx()
    }

    private fun startTornadoFx() {
        log.info { "Starting up TornadoFX application." }
        FX.dicontainer = object : DIContainer, KoinComponent {
            override fun <T : Any> getInstance(type: KClass<T>): T {
                return getKoin().get(clazz = type, qualifier = null, parameters = null)
            }
        }
        launch<AllFitApp>(emptyArray())
    }
}
