package allfit

import mu.KotlinLogging.logger
import tornadofx.*

object Main {
    private val log = logger {}

    @JvmStatic
    fun main(args: Array<String>) {
        log.info { "Starting up AllFit ..." }
        launch<AllFitApp>(args)
    }
}

class AllFitApp : App(RootView::class, Styles::class)
