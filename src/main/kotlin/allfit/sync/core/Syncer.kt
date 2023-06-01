package allfit.sync.core

import mu.KotlinLogging.logger

interface Syncer {
    fun registerListener(listener: SyncListener)
    fun syncAll()
}

class NoOpSyncer(
    private val listeners: SyncListenerManager
) : Syncer, SyncListenerManager by listeners {
    private val log = logger {}

    override fun syncAll() {
        log.info { "No-op syncer is not doing anything." }
        listeners.onSyncStart(listOf("No operation dummy syncer."))
        listeners.onSyncStepDone(0)
        listeners.onSyncEnd()
    }
}

