package allfit.sync

import allfit.api.OnefitClient
import allfit.api.models.CategoryJson
import allfit.domain.Category
import allfit.persistence.CategoriesRepo
import mu.KotlinLogging.logger

interface Syncer {
    suspend fun sync()
}

class RealSyncer(
    private val categoriesRepo: CategoriesRepo,
    private val client: OnefitClient
) : Syncer {

    private val log = logger {}

    override suspend fun sync() {
        log.info { "Sync started ..." }
        syncCategories()
    }

    private suspend fun syncCategories() {
        log.debug { "Sync categories." }
        val localCategories = categoriesRepo.load()
        val remoteCategories = client.getCategories()
        val report = SyncDiffer.diffCategories(localCategories, remoteCategories)

        if (report.toInsert.isNotEmpty()) {
            categoriesRepo.insert(report.toInsert.map { it.toCategory() })
        }
        if (report.toDelete.isNotEmpty()) {
            categoriesRepo.delete(report.toDelete.map { it.id })
        }
    }
}

private fun CategoryJson.toCategory() =
    Category(
        id = id,
        name = name,
    )

object NoOpSyncer : Syncer {

    private val log = logger {}

    override suspend fun sync() {
        log.info { "No-op syncer is not doing anything." }
    }
}
