package allfit.sync.view

import allfit.presentation.Styles
import javafx.scene.control.Label
import javafx.scene.control.TextArea
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import javafx.stage.Stage
import tornadofx.App
import tornadofx.View
import tornadofx.label
import tornadofx.launch
import tornadofx.singleAssign
import tornadofx.textarea
import tornadofx.vbox
import tornadofx.vgrow

class SyncFxWindowApp : App(
    primaryView = SyncFxWindow::class,
    stylesheet = Styles::class,
) {

    private val window by inject<SyncFxWindow>()

    override fun start(stage: Stage) {
        super.start(stage)
        window.initSteps(listOf("Foo", "Bar", "Baz"))
        window.addDetailMessage("Some detail message ...")
    }
}

class SyncFxWindow : View() {

    private var messageTextArea by singleAssign<TextArea>()
    private var stepsVbox by singleAssign<VBox>()
    private val stepLabels = mutableListOf<Label>()

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            launch<SyncFxWindowApp>()
        }
    }

    private enum class StepState(val icon: String) {
        Idle("❔"), Working("⏳"), Done("✅"),
    }

    init {
        title = "Synchronizing OneFit data ..."
    }

    override val root = vbox {
        stepsVbox = vbox {
            vgrow = Priority.NEVER
        }
        messageTextArea = textarea {
            isEditable = false
            vgrow = Priority.ALWAYS
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