package allfit.presentation

import allfit.presentation.view.MainView
import javafx.stage.Stage
import tornadofx.App
import tornadofx.find

class TornadoFxEntryPoint : App(
    primaryView = MainView::class,
    stylesheet = Styles::class,
) {
    private val log = mu.KotlinLogging.logger {}

    init {
        registerEagerSingletons()
    }

    override fun start(stage: Stage) {
        log.info { "start(stage)" }
        super.start(stage)
        fire(ApplicationStartedFxEvent)
    }

    private fun registerEagerSingletons() {
        find(MainController::class)
    }
}
