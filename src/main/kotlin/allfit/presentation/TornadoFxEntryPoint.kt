package allfit.presentation

import allfit.presentation.logic.MainController
import allfit.presentation.view.MainView
import javafx.application.Platform
import javafx.stage.Stage
import mu.KotlinLogging.logger
import tornadofx.*

class TornadoFxEntryPoint : App(
    primaryView = MainView::class,
    stylesheet = Styles::class,
) {
    private val log = logger {}

    init {
        registerEagerSingletons()
    }

    override fun start(stage: Stage) {
        log.info { "start(stage)" }
        super.start(stage)
        fire(ApplicationStartedFxEvent)

        Thread.setDefaultUncaughtExceptionHandler { _, throwable ->
            Platform.runLater {
                showErrorDialog(throwable)
            }
        }
    }

    private fun showErrorDialog(throwable: Throwable) {
        log.error(throwable) { "Uncaught exception detected!" }
        val dialog = ErrorDialog(throwable)
        dialog.dialog("Fatal error!") {
        }
    }

    private fun registerEagerSingletons() {
        find(MainController::class)
    }
}

class ErrorDialog(private val throwable: Throwable) : View() {
    override val root = vbox {
        textarea(throwable.stackTraceToString())
    }

}
