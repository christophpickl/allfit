package allfit.sync.view

import allfit.presentation.ApplicationStartedFxEvent
import allfit.sync.core.SyncListener
import allfit.sync.core.Syncer
import javafx.stage.Modality
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging.logger
import tornadofx.Controller
import tornadofx.runLater

class SyncController : Controller(), SyncListener {

    private val syncer by di<Syncer>()
    private val syncWindow by inject<SyncFxWindow>()
    private val logger = logger {}

    init {
        subscribe<ApplicationStartedFxEvent>() {
            logger.info { "Start sync via view..." }
            syncWindow.openWindow(modality = Modality.NONE)
            syncer.registerListener(this@SyncController)
            runAsync {
                syncer.syncAll() // TODO catch exceptions?!
            }
        }
    }

    override fun onSyncStart(steps: List<String>) {
        logger.debug { "sync start: $steps" }
        runLater {
            syncWindow.initSteps(steps)
        }
    }

    override fun onSyncStepDone(currentStep: Int) {
        logger.debug { "sync step done: $currentStep" }
        runLater {
            syncWindow.stepDone(currentStep)
        }
    }

    override fun onSyncDetail(message: String) {
        logger.debug { "sync detail: $message" }
        runLater {
            syncWindow.addDetailMessage(message)
        }
    }

    override fun onSyncEnd() {
        logger.debug { "sync done" }
        runLater {
            runBlocking {
                delay(2_000)
                syncWindow.close()
            }
        }
    }
}
