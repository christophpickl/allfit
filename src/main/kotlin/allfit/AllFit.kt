package allfit

import allfit.view.AllFitApp
import mu.KotlinLogging.logger
import org.koin.core.component.KoinComponent
import org.koin.core.context.startKoin
import tornadofx.DIContainer
import tornadofx.FX
import tornadofx.launch
import kotlin.reflect.KClass

object AllFit {
    private val log = logger {}

    @JvmStatic
    fun main(args: Array<String>) {
        log.info { "Starting up AllFit ..." }

        val koin = startKoin {
            modules(mainModule(args))
        }.koin
        koin.get<AllFitStarter>().start()
    }
}

class AllFitStarter(private val cliArgs: Array<String>) {

    private val log = logger {}

    fun start() {
        log.info { "Starting TornadoFX application." }

        FX.dicontainer = object : DIContainer, KoinComponent {
            override fun <T : Any> getInstance(type: KClass<T>): T {
                return getKoin().get(clazz = type, qualifier = null, parameters = null)
            }
        }

        launch<AllFitApp>(cliArgs)
    }
}
