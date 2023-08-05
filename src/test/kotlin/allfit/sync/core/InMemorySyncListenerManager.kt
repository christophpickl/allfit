package allfit.sync.core

class InMemorySyncListenerManager : SyncListenerManager {

    val syncStarts = mutableListOf<List<String>>()
    val syncStepDones = mutableListOf<Int>()
    val syncDetails = mutableListOf<String>()
    val syncEnds = mutableListOf<Unit>()

    override fun registerListener(listener: SyncListener) {
    }

    override fun onSyncStart(steps: List<String>) {
        syncStarts += steps
    }

    override fun onSyncStepDone(currentStep: Int) {
        syncStepDones += currentStep
    }

    override fun onSyncDetail(message: String) {
        syncDetails += message
    }

    override fun onSyncEnd() {
        syncEnds += Unit
    }
}
