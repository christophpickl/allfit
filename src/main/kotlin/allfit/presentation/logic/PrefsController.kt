package allfit.presentation.logic

import allfit.service.Prefs
import allfit.service.PrefsData
import javafx.stage.Stage
import mu.KotlinLogging.logger
import tornadofx.Controller

class PrefsController : Controller() {

    private val logger = logger {}
    private val prefs by di<Prefs>()

    fun prepareStage(stage: Stage) {
        stage.setOnCloseRequest {
            logger.debug { "Close requested." }
            store(stage)
        }
        load(stage)
    }

    private fun load(stage: Stage) {
        val data = prefs.load()
        stage.x = data.windowX
        stage.y = data.windowY
        stage.width = data.windowWidth
        stage.height = data.windowHeight
    }

    private fun store(stage: Stage) {
        prefs.store(
            PrefsData(
                windowX = stage.x,
                windowY = stage.y,
                windowWidth = stage.width,
                windowHeight = stage.height,
            )
        )
    }
}
