package allfit.sync

import allfit.api.models.CategoriesJson
import allfit.api.models.CategoryJson
import allfit.persistence.CategoryDbo

object SyncDiffer {
    fun diffCategories(local: List<CategoryDbo>, remote: CategoriesJson): DiffReport<CategoryJson, CategoryDbo> {
        val localById = local.associateBy { it.id }
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
            toInsert = toBeInserted.values.toList(),
            toDelete = toBeDeleted.values.toList(),
        )
    }
}

data class DiffReport<INSERT, DELETE>(
    val toInsert: List<INSERT>,
    val toDelete: List<DELETE>,
)
