package allfit.sync.core

import io.github.oshai.kotlinlogging.KotlinLogging.logger

class DelayedSyncer(
    private val listeners: SyncListenerManager
) : Syncer, SyncListenerManager by listeners {

    private val log = logger {}
    private val steps = List(8) { "Step ${it + 1}" }

    override fun syncAll() {
        log.info { "Delayed syncer is running..." }
        listeners.onSyncStart(steps)
        steps.forEachIndexed { index, s ->
            listeners.onSyncDetail("Detail #1 for step: $s")
            Thread.sleep(500)
            listeners.onSyncDetail("Detail #2 for step: $s")
            Thread.sleep(500)
//            if(index == 2) {
//                error("deliberate exception thrown")
//            }
            listeners.onSyncDetail("Detail #3 for step: $s")
            Thread.sleep(500)
            listeners.onSyncStepDone(index)
        }
        listeners.onSyncEnd()
    }
}