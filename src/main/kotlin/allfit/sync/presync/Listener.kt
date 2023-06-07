package allfit.sync.presync

interface PreSyncListener {
    fun onSyncStart(steps: List<String>)

    /**
     * @param currentStep 0-base indexed
     */
    fun onSyncStepDone(currentStep: Int)
    fun onSyncDetail(message: String)
    fun onSyncEnd()
}

interface PreSyncListenerManager : PreSyncListener {
    fun registerListener(listener: PreSyncListener)
}

class PreSyncListenerManagerImpl : PreSyncListenerManager {

    private val listeners = mutableListOf<PreSyncListener>()

    override fun registerListener(listener: PreSyncListener) {
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
