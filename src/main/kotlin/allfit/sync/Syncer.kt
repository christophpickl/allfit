package allfit.sync

import allfit.api.OnefitClient
import allfit.api.PartnerSearchParams
import allfit.api.models.CategoriesJson
import allfit.api.models.CategoryJsonDefinition
import allfit.api.models.PartnerCategoryJson
import allfit.api.models.PartnerJson
import allfit.api.models.PartnersJson
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

object NoOpSyncer : Syncer {
    private val log = logger {}
    override suspend fun syncAll() {
        log.info { "No-op syncer is not doing anything." }
    }
}

class RealSyncer(
    private val client: OnefitClient,
    private val categoriesRepo: CategoriesRepo,
    private val partnersRepo: PartnersRepo,
) : Syncer {

    private val log = logger {}

    override suspend fun syncAll() {
        log.info { "Sync started ..." }
        val partners = client.getPartners(PartnerSearchParams.simple())
        syncAny(categoriesRepo, mergedCategories(client.getCategories(), partners)) { it.toCategoryDbo() }
        syncAny(partnersRepo, partners.data) { it.toPartnerDbo() }
    }
}

private fun mergedCategories(categories: CategoriesJson, partners: PartnersJson) =
    mutableMapOf<Int, CategoryJsonDefinition>().apply {
        putAll(categories.data.associateBy { it.id })
        putAll(partners.toFlattenedCategories().associateBy { it.id })
    }.values.toList()

private fun <
        REPO : Repo<DBO>,
        DBO : Dbo,
        ENTITY : SyncableJsonEntity
        > syncAny(repo: REPO, syncableJsons: List<ENTITY>, mapper: (ENTITY) -> DBO) {
    val localDbos = repo.select()
    val report = SyncDiffer.diff(localDbos, syncableJsons)

    if (report.toInsert.isNotEmpty()) {
        repo.insert(report.toInsert.map(mapper))
    }
    if (report.toDelete.isNotEmpty()) {
        repo.delete(report.toDelete.map { it.id })
    }
}

private fun PartnersJson.toFlattenedCategories() = data.map { partner ->
    mutableListOf<PartnerCategoryJson>().also {
        it.add(partner.category)
        it.addAll(partner.categories)
    }
}.flatten()

private fun PartnerJson.toPartnerDbo() =
    PartnerDbo(
        id = id,
        name = name,
        isDeleted = false,
        categories = emptyList(), // FIXME
    )

private fun CategoryJsonDefinition.toCategoryDbo() =
    CategoryDbo(
        id = id,
        name = name,
        isDeleted = false,
    )
