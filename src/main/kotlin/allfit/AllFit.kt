package allfit

import allfit.api.ClassPathOnefitClient
import allfit.api.OnefitClient
import allfit.api.authenticateOneFit
import allfit.presentation.TornadoFxEntryPoint
import allfit.presentation.UiSyncer
import allfit.service.CredentialsLoader
import kotlinx.coroutines.runBlocking
import mu.KLogger
import mu.KotlinLogging.logger
import org.koin.core.Koin
import org.koin.core.component.KoinComponent
import org.koin.core.context.startKoin
import tornadofx.DIContainer
import tornadofx.FX
import tornadofx.launch
import kotlin.reflect.KClass

object AllFit {

    private var log: KLogger

    init {
        reconfigureLog()
        log = logger {}
    }

    @JvmStatic
    fun main(args: Array<String>) {
        val log = logger {}
        log.info {
            """Starting up AllFit.
 ______   _        _        ______ _____ _______ 
| |  | | | |      | |      | |      | |    | |   
| |__| | | |   _  | |   _  | |----  | |    | |   
|_|  |_| |_|__|_| |_|__|_| |_|     _|_|_   |_|   
"""
        }
        runBlocking {
            startupKoin().get<AllFitStarter>().start()
        }
    }

    private suspend fun startupKoin(): Koin {
        val config = if (Environment.current == Environment.Development) AppConfig.develop else AppConfig.prod
        val client = buildClient(config)
        return startKoin {
            log.debug { "Starting up Koin context." }
            modules(rootModule(config, client))
        }.koin
    }

    private suspend fun buildClient(config: AppConfig): OnefitClient =
        if (config.mockClient) {
            ClassPathOnefitClient
        } else {
            authenticateOneFit(CredentialsLoader.load())
        }
}

class AllFitStarter(private val uiSyncer: UiSyncer) {

    private val log = logger {}

    fun start() {
        uiSyncer.start {
            startTornadoFx()
        }
    }

    private fun startTornadoFx() {
        log.info { "Starting up TornadoFX application." }
        FX.dicontainer = object : DIContainer, KoinComponent {
            override fun <T : Any> getInstance(type: KClass<T>): T {
                return getKoin().get(clazz = type, qualifier = null, parameters = null)
            }
        }
        launch<TornadoFxEntryPoint>(emptyArray())
    }
}
