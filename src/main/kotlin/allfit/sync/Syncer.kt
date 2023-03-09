package allfit.sync

import allfit.api.OnefitClient
import allfit.persistence.CategoriesRepo

interface Syncer {
    suspend fun sync()
}

class RealSyncer(
    private val repo: CategoriesRepo,
    private val client: OnefitClient
) : Syncer {
    override suspend fun sync() {
        val localCategories = repo.load()
        val remoteCategories = client.getPartnersCategories()
        val report = SyncDiffer.diffCategories(localCategories, remoteCategories)

        // FIXME execute DB operations
    }
}

object NoOpSyncer : Syncer {
    override suspend fun sync() {
        println("NoOp sync")
    }
}
