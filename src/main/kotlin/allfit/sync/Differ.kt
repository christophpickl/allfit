package allfit.sync

import allfit.api.models.PartnerCategoriesJson
import allfit.api.models.PartnerCategoryJson
import allfit.domain.Categories
import allfit.domain.Category

object SyncDiffer {
    fun diffCategories(local: Categories, remote: PartnerCategoriesJson): DiffReport<PartnerCategoryJson, Category> {
        val localById = local.categories.associateBy { it.id }
        val remoteById = remote.data.associateBy { it.id }
        val toBeInserted = remoteById.toMutableMap()
        localById.forEach { (i, _) ->
            toBeInserted.remove(i)
        }
        val toBeDeleted = localById.toMutableMap()
        remoteById.forEach { (i, _) ->
            toBeDeleted.remove(i)
        }
        return DiffReport(
            insert = toBeInserted.values.toList(),
            delete = toBeDeleted.values.toList(),
        )
    }
}

data class DiffReport<INSERT, DELETE>(
    val insert: List<INSERT>,
    val delete: List<DELETE>,
)
