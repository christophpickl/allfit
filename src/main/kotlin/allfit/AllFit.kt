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
import javax.swing.JOptionPane
import kotlin.reflect.KClass

object AllFit {

    private val config = if (Environment.current == Environment.Development) AppConfig.develop else AppConfig.prod
    private val log: KLogger

    init {
        reconfigureLog(config.useFileAppender)
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
            try {
                startupKoin().get<AllFitStarter>().start()
            } catch (e: Exception) {
                log.error(e) { "Startup failed!" }
                showStartupError(e)
            }
        }
    }

    private suspend fun startupKoin(): Koin {
        val client = buildClient(config)
        return startKoin {
            log.debug { "Starting up Koin context." }
            modules(rootModule(config, client))
        }.koin
    }

    private suspend fun buildClient(config: AppConfig): OnefitClient = if (config.mockClient) {
        ClassPathOnefitClient
    } else {
        authenticateOneFit(CredentialsLoader.load())
    }
}

class AllFitStarter(private val uiSyncer: UiSyncer) {

    private val log = logger {}

    fun start() {
        uiSyncer.start { result ->
            result.fold(
                onSuccess = {
                    startTornadoFx()
                },
                onFailure = {
                    log.error(it) { "Sync failed!" }
                    showStartupError(it)
                },
            )
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

private fun showStartupError(exception: Throwable) {
    JOptionPane.showMessageDialog(
        null, exception.message ?: "See log for details.", "Startup Error!", JOptionPane.ERROR_MESSAGE
    )
}
