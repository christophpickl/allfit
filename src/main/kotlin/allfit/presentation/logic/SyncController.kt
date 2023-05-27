package allfit.presentation.logic

import allfit.presentation.ApplicationStartedFxEvent
import allfit.sync.SyncListener
import allfit.sync.Syncer
import javafx.scene.control.Label
import javafx.scene.control.TextArea
import javafx.scene.layout.VBox
import javafx.stage.Modality
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging.logger
import tornadofx.Controller
import tornadofx.View
import tornadofx.label
import tornadofx.runLater
import tornadofx.singleAssign
import tornadofx.textarea
import tornadofx.vbox

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

class SyncFxWindow : View() {

    private var messageTextArea by singleAssign<TextArea>()
    private var stepsVbox by singleAssign<VBox>()
    private val stepLabels = mutableListOf<Label>()

    private enum class StepState(val icon: String) {
        Idle("❔"),
        Working("⏳"),
        Done("✅"),
    }

    init {
        title = "Synchronizing OneFit data ..."
    }

    override val root = vbox {
        stepsVbox = vbox { }
        messageTextArea = textarea {
            isFillWidth = true
            minHeight = 300.0
        }
    }

    fun addDetailMessage(message: String) {
        messageTextArea.text = message + "\n" + messageTextArea.text
    }

    fun initSteps(steps: List<String>) {
        steps.forEach { step ->
            val child = label("${StepState.Idle.icon} $step")
            stepLabels += child
            stepsVbox.add(child)
        }
        changeStep(0, StepState.Working)
    }

    fun stepDone(stepNumber: Int) {
        changeStep(stepNumber, StepState.Done)
        if (stepNumber + 1 < stepLabels.size) {
            changeStep(stepNumber + 1, StepState.Working)
        }
    }

    private fun changeStep(number: Int, state: StepState) {
        stepLabels[number].text = state.icon + stepLabels[number].text.substring(1)
    }
}
