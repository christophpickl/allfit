package allfit.presentation

import allfit.presentation.components.ErrorDialog
import allfit.presentation.logic.MainController
import allfit.presentation.logic.ModelRepoBinder
import allfit.presentation.logic.PartnerUpdateController
import allfit.presentation.logic.PrefsController
import allfit.presentation.partners.PartnersController
import allfit.presentation.preferences.PreferencesController
import allfit.presentation.view.MainView
import allfit.service.Exporter
import allfit.sync.view.SyncController
import io.github.oshai.kotlinlogging.KotlinLogging.logger
import javafx.application.Platform
import javafx.stage.Stage
import kotlin.system.exitProcess
import tornadofx.App
import tornadofx.find

class TornadoFxEntryPoint : App(
    primaryView = MainView::class,
    stylesheet = Styles::class,
) {

    private val log = logger {}
    private val eagerSingletons = listOf(
        MainController::class,
        PartnerUpdateController::class,
        PartnersController::class,
        SyncController::class,
        NotesController::class,
        PreferencesController::class,
        Exporter::class,
    )

    init {
        eagerSingletons.forEach { singleton ->
            find(singleton)
        }
    }

    override fun start(stage: Stage) {
        log.info { "start(stage)" }
        find<ModelRepoBinder>().bindModels()
        super.start(stage)
        find<PrefsController>().prepareStage(stage)
        fire(ApplicationStartedFxEvent)

        Thread.setDefaultUncaughtExceptionHandler { _, throwable ->
            Platform.runLater {
                ErrorDialog.show(throwable)
            }
        }
    }

    override fun stop() {
        log.info { "stop()" }
        fire(ApplicationStoppingFxEvent)
        super.stop()
        exitProcess(0)
    }
}
