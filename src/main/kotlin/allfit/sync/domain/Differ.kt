package allfit.sync.domain

import allfit.api.models.SyncableJson
import allfit.persistence.HasIntId

object Differ {
    fun <ENTITY : HasIntId, JSON : SyncableJson> diff(
        localDbos: List<ENTITY>,
        syncableJsons: List<JSON>,
        mapper: (JSON) -> ENTITY,
    ): DiffReport<ENTITY, ENTITY> {
        val localById = localDbos.associateBy { it.id }
        val remoteById = syncableJsons.associateBy { it.id }
        return DiffReport(
            toInsert = calcToBeInserted(localById, remoteById, mapper),
            toDelete = calcToBeDeleted(localById, remoteById),
        )
    }

    private fun <DOMAIN : HasIntId, ENTITY : SyncableJson> calcToBeInserted(
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

    private fun <DOMAIN : HasIntId, ENTITY : SyncableJson> calcToBeDeleted(
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
