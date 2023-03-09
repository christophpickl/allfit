package allfit.api.models

interface SyncableJsonContainer<ENTITY : SyncableJsonEntity> {
    val data: List<ENTITY>
}

interface SyncableJsonEntity {
    val id: Int
}
