package allfit

import allfit.api.ClassPathOnefitClient
import allfit.api.Credentials
import allfit.api.OnefitClient
import allfit.api.authenticateOneFit
import allfit.domain.Location
import allfit.persistence.domain.SinglesRepo
import allfit.presentation.TornadoFxEntryPoint
import allfit.presentation.UiPreSyncer
import allfit.service.SystemClock
import allfit.service.credentials.CredentialsManager
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging.logger
import javax.swing.JOptionPane
import kotlin.reflect.KClass
import kotlinx.coroutines.runBlocking
import org.koin.core.Koin
import org.koin.core.component.KoinComponent
import org.koin.core.context.startKoin
import tornadofx.DIContainer
import tornadofx.FX
import tornadofx.launch

object AllFit {

    private val config = if (Environment.current == Environment.Development) AppConfig.develop else AppConfig.prod
    private val log: KLogger

    init {
        reconfigureLog(config.useLogFileAppender)
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
                val locationAndCredentials = CredentialsManager.load()
                val location = locationAndCredentials.location
                startupKoin(locationAndCredentials.other).get<AllFitStarter>().start(location)
            } catch (e: Exception) {
                log.error(e) { "Startup failed!" }
                showStartupError(e)
            }
        }
    }

    private suspend fun startupKoin(credentials: Credentials): Koin {
        val client = buildClient(config, credentials)
        return startKoin {
            log.debug { "Starting up Koin context." }
            modules(rootModule(config, client))
        }.koin
    }

    private suspend fun buildClient(config: AppConfig, credentials: Credentials): OnefitClient =
        if (config.mockClient) {
            ClassPathOnefitClient
        } else {
            authenticateOneFit(credentials, clock = SystemClock)
        }
}

class AllFitStarter(
    private val preSyncEnabled: Boolean,
    private val uiPreSyncer: UiPreSyncer,
    private val singlesRepo: SinglesRepo,
) {

    private val log = logger {}

    fun start(location: Location?) {
        if (location != null) {
            singlesRepo.updateLocation(location)
        }
        if (preSyncEnabled) {
            uiPreSyncer.start { result ->
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
        } else {
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

private fun showStartupError(exception: Throwable) {
    JOptionPane.showMessageDialog(
        null, exception.message ?: "See log for details.", "Startup Error!", JOptionPane.ERROR_MESSAGE
    )
}
