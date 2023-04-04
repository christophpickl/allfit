package allfit.sync

import allfit.api.JsonLogFileManager
import allfit.api.OnefitClient
import allfit.api.PartnerSearchParams
import allfit.api.models.PartnersJsonRoot
import allfit.api.models.SyncableJson
import allfit.persistence.BaseRepo
import allfit.persistence.HasIntId
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging.logger
import org.jetbrains.exposed.sql.transactions.transaction

interface Syncer {
    fun registerListener(listener: SyncListener)
    fun syncAll()
}

interface SyncListener {
    fun onSyncStart(steps: List<String>)
    fun onSyncStepDone()
    fun onSyncDetail(message: String)
    fun onSyncEnd()
}

class NoOpSyncer(
    private val listeners: SyncListenerManager
) : Syncer, SyncListenerManager by listeners {
    private val log = logger {}

    override fun syncAll() {
        log.info { "No-op syncer is not doing anything." }
        listeners.onSyncStart(listOf("No operation dummy syncer."))
        listeners.onSyncStepDone()
        listeners.onSyncEnd()
    }
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

    override fun onSyncStepDone() {
        listeners.forEach {
            it.onSyncStepDone()
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

class DelayedSyncer(
    private val listeners: SyncListenerManager
) : Syncer, SyncListenerManager by listeners {

    private val log = logger {}
    private val steps = listOf("First step", "Second step", "Third step", "Second last step", "Last step")

    override fun syncAll() {
        log.info { "Delayed syncer is running..." }
        listeners.onSyncStart(steps)
        steps.forEach {
            listeners.onSyncDetail("Detail #1 for step: $it")
            Thread.sleep(1500)
            listeners.onSyncDetail("Detail #2 for step: $it")
            Thread.sleep(1500)
            listeners.onSyncDetail("Detail #3 for step: $it")
            Thread.sleep(1500)
            listeners.onSyncStepDone()
        }
        listeners.onSyncEnd()
    }
}

private data class SyncStep(
    val message: String,
    val code: suspend () -> Unit,
)

class CompositeSyncer(
    private val client: OnefitClient,
    private val categoriesSyncer: CategoriesSyncer,
    private val partnersSyncer: PartnersSyncer,
    private val locationsSyncer: LocationsSyncer,
    private val workoutsSyncer: WorkoutsSyncer,
    private val reservationsSyncer: ReservationsSyncer,
    private val checkinsSyncer: CheckinsSyncer,
    private val listeners: SyncListenerManager,
    private val jsonLogFileManager: JsonLogFileManager,
) : Syncer, SyncListenerManager by listeners {

    private lateinit var partners: PartnersJsonRoot
    private val log = logger {}

    private val syncSteps = listOf(
        SyncStep("Fetching partners") {
            partners = client.getPartners(PartnerSearchParams.simple())
        },
        SyncStep("Syncing categories") {
            categoriesSyncer.sync(partners)
        },
        SyncStep("Syncing partners") {
            partnersSyncer.sync(partners)
        },
        SyncStep("Syncing locations") {
            locationsSyncer.sync(partners)
        },
        SyncStep("Syncing workouts") {
            workoutsSyncer.sync()
        },
        SyncStep("Syncing reservations") {
            reservationsSyncer.sync()
        },
        SyncStep("Syncing checkins") {
            checkinsSyncer.sync()
        },
        SyncStep("Cleanup logs") {
            jsonLogFileManager.deleteOldLogs()
        },
    )

    override fun syncAll() {
        transaction {
            listeners.onSyncStart(syncSteps.map { it.message })
            runBlocking {
                log.info { "Sync started ..." }
                syncSteps.forEach {
                    log.debug { "Executing sync step: ${it.message}" }
                    it.code()
                    listeners.onSyncStepDone()
                }
            }
        }
        listeners.onSyncEnd()
    }
}

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

