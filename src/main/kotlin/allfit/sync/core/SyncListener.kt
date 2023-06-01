package allfit.sync.core

interface SyncListener {
    fun onSyncStart(steps: List<String>)

    /* 0-base indexed */
    fun onSyncStepDone(currentStep: Int)
    fun onSyncDetail(message: String)
    fun onSyncEnd()
}

interface SyncListenerManager : SyncListener {
    fun registerListener(listener: SyncListener)
}

class SyncListenerManagerImpl : SyncListenerManager {

    private val listeners = mutableListOf<SyncListener>()

    override fun registerListener(listener: SyncListener) {
        listeners += listener
    }

    override fun onSyncStart(steps: List<String>) {
        listeners.forEach {
            it.onSyncStart(steps)
        }
    }

    override fun onSyncStepDone(currentStep: Int) {
        listeners.forEach {
            it.onSyncStepDone(currentStep)
        }
    }

    override fun onSyncDetail(message: String) {
        listeners.forEach {
            it.onSyncDetail(message)
        }
    }

    override fun onSyncEnd() {
        listeners.forEach {
            it.onSyncEnd()
        }
    }
}
