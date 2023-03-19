package allfit.sync

import allfit.api.OnefitClient
import allfit.api.PartnerSearchParams
import allfit.api.models.SyncableJson
import allfit.domain.HasIntId
import allfit.persistence.BaseRepo
import mu.KotlinLogging.logger

interface Syncer {
    suspend fun syncAll()
}

object NoOpSyncer : Syncer {
    private val log = logger {}
    override suspend fun syncAll() {
        log.info { "No-op syncer is not doing anything." }
    }
}

class RealSyncer(
    private val client: OnefitClient,
    private val categoriesSyncer: CategoriesSyncer,
    private val partnersSyncer: PartnersSyncer,
    private val locationsSyncer: LocationsSyncer,
    private val workoutsSyncer: WorkoutsSyncer,
    private val reservationsSyncer: ReservationsSyncer,
) : Syncer {

    private val log = logger {}

    override suspend fun syncAll() {
        log.info { "Sync started ..." }
        // TODO wrap with transaction all of it (write test first which breaks in between)
        val partners = client.getPartners(PartnerSearchParams.simple())
        categoriesSyncer.sync(partners)
        partnersSyncer.sync(partners)
        locationsSyncer.sync(partners)
        workoutsSyncer.sync()
        reservationsSyncer.sync()
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

