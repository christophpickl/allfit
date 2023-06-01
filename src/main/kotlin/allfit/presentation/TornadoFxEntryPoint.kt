package allfit.presentation

import allfit.presentation.logic.MainController
import allfit.presentation.view.MainView
import allfit.sync.view.SyncController
import javafx.application.Platform
import javafx.stage.Stage
import mu.KotlinLogging.logger
import tornadofx.App
import tornadofx.View
import tornadofx.find
import tornadofx.textarea
import tornadofx.vbox

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
            textarea(throwable.stackTraceToString())
//            button("Dismiss") {
//                action {
//                    dialog.close()
//                }
//            }
        }
    }

    private fun registerEagerSingletons() {
        find(MainController::class)
        find(SyncController::class)
    }
}

class ErrorDialog(private val throwable: Throwable) : View() {
    override val root = vbox {
        // nope, this has no effect
//        textarea(throwable.stackTraceToString())
    }
}
