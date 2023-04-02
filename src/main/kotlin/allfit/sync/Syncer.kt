package allfit.sync

import allfit.api.OnefitClient
import allfit.api.PartnerSearchParams
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
    fun onSyncStep(stepNumber: Int, stepsTotalCount: Int, message: String)
    fun onSyncDone()
}

object NoOpSyncer : Syncer {
    private val log = logger {}
    private val listeners = mutableListOf<SyncListener>()
    override fun registerListener(listener: SyncListener) {
        listeners += listener
    }

    override fun syncAll() {
        log.info { "No-op syncer is not doing anything." }
        listeners.forEach {
            it.onSyncDone()
        }
    }
}

object DelayedSyncer : Syncer {
    private val log = logger {}
    private val listeners = mutableListOf<SyncListener>()
    override fun registerListener(listener: SyncListener) {
        listeners += listener
    }

    override fun syncAll() {
        log.info { "Delayed syncer is running..." }
        (1..5).forEach { step ->
            listeners.forEach {
                it.onSyncStep(step, 5, "Step $step working...")
            }
            Thread.sleep(1000 * 1)
        }
        listeners.forEach {
            it.onSyncDone()
        }
    }
}

class CompositeSyncer(
    private val client: OnefitClient,
    private val categoriesSyncer: CategoriesSyncer,
    private val partnersSyncer: PartnersSyncer,
    private val locationsSyncer: LocationsSyncer,
    private val workoutsSyncer: WorkoutsSyncer,
    private val reservationsSyncer: ReservationsSyncer,
    private val checkinsSyncer: CheckinsSyncer,
) : Syncer {

    private val log = logger {}
    private val totalSteps = 7
    private val listeners = mutableListOf<SyncListener>()

    override fun registerListener(listener: SyncListener) {
        listeners += listener
    }

    override fun syncAll() {
        var currentStep = 1
        transaction {
            runBlocking {
                log.info { "Sync started ..." }
                broadcastStep(currentStep++, "Fetching partners...")
                val partners = client.getPartners(PartnerSearchParams.simple())
                broadcastStep(currentStep++, "Syncing categories...")
                categoriesSyncer.sync(partners)
                broadcastStep(currentStep++, "Syncing partners...")
                partnersSyncer.sync(partners)
                broadcastStep(currentStep++, "Syncing locations...")
                locationsSyncer.sync(partners)
                broadcastStep(currentStep++, "Syncing workouts...")
                workoutsSyncer.sync()
                broadcastStep(currentStep++, "Syncing reservations...")
                reservationsSyncer.sync()
                broadcastStep(currentStep++, "Syncing checkins...")
                checkinsSyncer.sync()
            }
        }
        listeners.forEach {
            it.onSyncDone()
        }
    }

    private fun broadcastStep(currentStep: Int, message: String) {
        listeners.forEach {
            it.onSyncStep(currentStep, totalSteps, message)
        }
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

