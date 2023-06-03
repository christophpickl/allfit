package allfit.presentation

import allfit.sync.presync.NoOpPreSyncer
import allfit.sync.presync.PreSyncListener
import allfit.sync.presync.PreSyncListenerManagerImpl
import allfit.sync.presync.PreSyncer
import java.awt.BorderLayout
import java.awt.Cursor
import java.awt.Dimension
import javax.swing.BoxLayout
import javax.swing.JDialog
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JProgressBar
import javax.swing.JScrollPane
import javax.swing.JTextArea
import javax.swing.SwingUtilities
import mu.KotlinLogging.logger

class UiPreSyncer(private val preSyncer: PreSyncer) {

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            UiPreSyncer(NoOpPreSyncer(PreSyncListenerManagerImpl())).start {
                println("done")
            }
        }
    }

    private val log = logger {}

    fun start(finishCallback: (Result<Unit>) -> Unit) {
        log.info { "Start syncing via UI. " }
        StatefulUiPreSyncer(preSyncer, finishCallback).start()
    }

}

private class StatefulUiPreSyncer(
    private val preSyncer: PreSyncer,
    private val finishCallback: (Result<Unit>) -> Unit,
) : PreSyncListener {

    private val log = logger {}
    private val syncDialog = SyncProgressDialog()
    private var syncSteps = emptyList<String>()

    init {
        preSyncer.registerListener(this)
    }

    fun start() {
        SwingUtilities.invokeLater {
            syncDialog.setLocationRelativeTo(null)
            syncDialog.isVisible = true
        }
        try {
            preSyncer.sync()
        } catch (e: Exception) {
            finish(e)
        }
    }

    override fun onSyncStart(steps: List<String>) {
        syncSteps = steps
        syncDialog.initSteps(steps)
    }

    override fun onSyncStepDone(currentStep: Int) {
        syncDialog.stepDone(currentStep)
    }

    override fun onSyncDetail(message: String) {
        syncDialog.detailMessage(message)
    }

    override fun onSyncEnd() {
        log.debug { "sync done." }
        finish(null)
    }

    private fun finish(exception: Exception?) {
        syncDialog.isVisible = false
        syncDialog.dispose()
        finishCallback(if (exception == null) Result.success(Unit) else Result.failure(exception))
    }
}

private class SyncProgressDialog : JDialog() {

    private val stepsPanel = JPanel()
    private val detailsText = JTextArea().apply {
        isEditable = false
    }
    private val progressBar = JProgressBar().apply {
        value = 0
        isVisible = true
        isIndeterminate = false
    }
    private val stepsLabels = mutableListOf<JLabel>()
    private val iconWorking = "⏳"
    private val iconDone = "✅"
    private val iconWaiting = "❔"

    init {
        title = "Presynchronizing OneFit data"
        cursor = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)
        stepsPanel.layout = BoxLayout(stepsPanel, BoxLayout.Y_AXIS)
        val northPanel = JPanel()
        northPanel.add(stepsPanel)

        val mainPanel = JPanel(BorderLayout())
        mainPanel.add(northPanel, BorderLayout.NORTH)
        mainPanel.add(JScrollPane(detailsText), BorderLayout.CENTER)
        mainPanel.add(progressBar, BorderLayout.SOUTH)
        rootPane.contentPane.add(mainPanel)
        size = Dimension(800, 500)
    }

    fun initSteps(steps: List<String>) {
        progressBar.minimum = 0
        progressBar.maximum = steps.size
        steps.forEachIndexed { index, step ->
            val label = JLabel().apply {
                text = "$iconWaiting - ${index + 1}: $step"
            }
            stepsLabels.add(label)
            stepsPanel.add(label)
        }
        updateStepIcon(0, iconWorking)
        revalidate()
    }

    fun stepDone(stepNumber: Int) {
        progressBar.value = stepNumber
        updateStepIcon(stepNumber, iconDone)
        if (stepNumber != (stepsLabels.size - 1)) {
            updateStepIcon(stepNumber + 1, iconWorking)
        }
        addDetail("Step ${stepNumber + 1}/${stepsLabels.size} done.")
    }

    private fun updateStepIcon(stepNumber: Int, icon: String) {
        val oldText = stepsLabels[stepNumber].text
        stepsLabels[stepNumber].text = icon + oldText.substring(1)
    }

    fun detailMessage(message: String) {
        addDetail(message)
    }

    private fun addDetail(message: String) {
        detailsText.text = message + "\n" + detailsText.text
    }
}
