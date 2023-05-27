package allfit.sync

import allfit.api.models.SyncableJson
import allfit.persistence.BaseRepo
import allfit.persistence.HasIntId

interface Syncer {
    fun registerListener(listener: SyncListener)
    fun syncAll()
}

//class NoOpSyncer(
//    private val listeners: SyncListenerManager
//) : Syncer, SyncListenerManager by listeners {
//    private val log = logger {}
//
//    override fun syncAll() {
//        log.info { "No-op syncer is not doing anything." }
//        listeners.onSyncStart(listOf("No operation dummy syncer."))
//        listeners.onSyncStepDone(0)
//        listeners.onSyncEnd()
//    }
//}

fun <
        REPO : BaseRepo<ENTITY>,
        ENTITY : HasIntId,
        JSON : SyncableJson
        > syncAny(
    repo: REPO,
    syncableJsons: List<JSON>,
    mapper: (JSON) -> ENTITY
): DiffReport<ENTITY, ENTITY> {
    val localDomains = repo.selectAll()
    val report = Differ.diff(localDomains, syncableJsons, mapper)

    if (report.toInsert.isNotEmpty()) {
        repo.insertAll(report.toInsert)
    }
    if (report.toDelete.isNotEmpty()) {
        repo.deleteAll(report.toDelete.map { it.id })
    }
    return report
}
