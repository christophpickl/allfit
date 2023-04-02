package allfit.presentation

import allfit.sync.DelayedSyncer
import allfit.sync.SyncListener
import allfit.sync.Syncer
import mu.KotlinLogging.logger
import java.awt.Dimension
import javax.swing.JDialog
import javax.swing.JLabel
import javax.swing.JPanel

class UiSyncer(private val syncer: Syncer) {

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            UiSyncer(DelayedSyncer).start {
                println("done")
            }
        }
    }

    private val log = logger {}

    fun start(finishCallback: () -> Unit) {
        log.info { "Start syncing via UI. " }
        StatefulUiSyncer(syncer, finishCallback).start()
    }

}

private class StatefulUiSyncer(
    private val syncer: Syncer,
    private val finishCallback: () -> Unit,
) : SyncListener {

    private val log = logger {}
    private val syncDialog = SyncProgressDialog()

    init {
        syncer.registerListener(this)
    }

    fun start() {
        syncDialog.setLocationRelativeTo(null)
        syncDialog.isVisible = true
        syncer.syncAll()
    }

    override fun onSyncStep(stepNumber: Int, stepsTotalCount: Int, message: String) {
        log.debug { "sync step: $stepNumber/$stepsTotalCount => $message" }
        syncDialog.setMessage("$stepNumber/$stepsTotalCount - $message")
    }

    override fun onSyncDone() {
        log.debug { "sync done." }
        syncDialog.isVisible = false
        syncDialog.dispose()
        finishCallback()
    }
}

private class SyncProgressDialog : JDialog() {

    private val messageLabel = JLabel()

    init {
        title = "Synchronizing"
        val panel = JPanel()
        messageLabel.text = "Idle"
        panel.add(messageLabel)
        rootPane.contentPane.add(panel)
        size = Dimension(600, 120)
    }

    fun setMessage(message: String) {
        messageLabel.text = message
    }
}
