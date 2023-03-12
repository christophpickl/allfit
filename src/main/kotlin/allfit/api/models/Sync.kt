package allfit.api.models

interface SyncableJsonContainer<ENTITY : SyncableJson> {
    val data: List<ENTITY>
}

interface SyncableJson {
    val id: Int
}
