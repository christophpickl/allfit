package allfit.sync

import allfit.api.models.SyncableJsonEntity
import allfit.persistence.Dbo

object SyncDiffer {
    fun <DBO : Dbo, ENTITY : SyncableJsonEntity> diff(
        localDbos: List<DBO>,
        syncableJsons: List<ENTITY>
    ): DiffReport<ENTITY, DBO> {
        val localById = localDbos.associateBy { it.id }
        val remoteById = syncableJsons.associateBy { it.id }

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
