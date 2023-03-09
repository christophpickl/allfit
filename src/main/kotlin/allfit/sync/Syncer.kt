package allfit.sync

import allfit.api.OnefitClient
import allfit.api.models.CategoryJson
import allfit.api.models.PartnerJson
import allfit.api.models.SyncableJsonContainer
import allfit.api.models.SyncableJsonEntity
import allfit.persistence.CategoriesRepo
import allfit.persistence.CategoryDbo
import allfit.persistence.Dbo
import allfit.persistence.PartnerDbo
import allfit.persistence.PartnersRepo
import allfit.persistence.Repo
import mu.KotlinLogging.logger

interface Syncer {
    suspend fun syncAll()
}

class RealSyncer(
    private val client: OnefitClient,
    private val categoriesRepo: CategoriesRepo,
    private val partnersRepo: PartnersRepo,
) : Syncer {

    private val log = logger {}

    override suspend fun syncAll() {
        log.info { "Sync started ..." }
        syncAny(categoriesRepo, client.getCategories()) { it.toCategoryDbo() }
        syncAny(partnersRepo, client.getPartners()) { it.toPartnerDbo() }
    }

    private fun <
            REPO : Repo<DBO>,
            DBO : Dbo,
            SYNC : SyncableJsonContainer<ENTITY>,
            ENTITY : SyncableJsonEntity
            > syncAny(repo: REPO, syncableJsons: SYNC, mapper: (ENTITY) -> DBO) {
        val localDbos = repo.select()
        val report = SyncDiffer.diff(localDbos, syncableJsons)

        if (report.toInsert.isNotEmpty()) {
            repo.insert(report.toInsert.map(mapper))
        }
        if (report.toDelete.isNotEmpty()) {
            repo.delete(report.toDelete.map { it.id })
        }
    }
}

private fun PartnerJson.toPartnerDbo() =
    PartnerDbo(
        id = id,
        name = name,
        isDeleted = false,
    )

private fun CategoryJson.toCategoryDbo() =
    CategoryDbo(
        id = id,
        name = name,
        isDeleted = false,
    )

object NoOpSyncer : Syncer {
    private val log = logger {}
    override suspend fun syncAll() {
        log.info { "No-op syncer is not doing anything." }
    }
}
