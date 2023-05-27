package allfit.sync.presync

interface PreSyncer {
    fun registerListener(listener: PreSyncListener)
    fun sync()
}

class NoOpPreSyncer(
    private val listeners: PreSyncListenerManager
) : PreSyncer, PreSyncListenerManager by listeners {
    override fun sync() {
        listeners.onSyncStart(listOf("Dummy"))
        listeners.onSyncStepDone(0)
        listeners.onSyncEnd()
    }
}

// TODO implement real pre-syncer (fetch period time + reservations (how many total, how many left)
