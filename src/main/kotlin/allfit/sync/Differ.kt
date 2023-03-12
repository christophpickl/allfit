package allfit.sync

import allfit.api.models.SyncableJsonEntity
import allfit.domain.HasIntId

object Differ {
    fun <DOMAIN : HasIntId, ENTITY : SyncableJsonEntity> diff(
        localDbos: List<DOMAIN>,
        syncableJsons: List<ENTITY>,
        mapper: (ENTITY) -> DOMAIN,
    ): DiffReport<DOMAIN, DOMAIN> {
        val localById = localDbos.associateBy { it.id }
        val remoteById = syncableJsons.associateBy { it.id }
        return DiffReport(
            toInsert = calcToBeInserted(localById, remoteById, mapper),
            toDelete = calcToBeDeleted(localById, remoteById),
        )
    }

    private fun <DOMAIN : HasIntId, ENTITY : SyncableJsonEntity> calcToBeInserted(
        localById: Map<Int, DOMAIN>,
        remoteById: Map<Int, ENTITY>,
        mapper: (ENTITY) -> DOMAIN
    ): List<DOMAIN> {
        val remotesToBeInserted = remoteById.toMutableMap()
        localById.forEach { (key, _) ->
            remotesToBeInserted.remove(key)
        }
        return remotesToBeInserted.values.map(mapper)
    }

    private fun <DOMAIN : HasIntId, ENTITY : SyncableJsonEntity> calcToBeDeleted(
        localById: Map<Int, DOMAIN>,
        remoteById: Map<Int, ENTITY>
    ): List<DOMAIN> {
        val toBeDeleted = localById.toMutableMap()
        remoteById.forEach { (i, _) ->
            toBeDeleted.remove(i)
        }
        return toBeDeleted.values.toList()
    }
}

data class DiffReport<INSERT, DELETE>(
    val toInsert: List<INSERT>,
    val toDelete: List<DELETE>,
)
