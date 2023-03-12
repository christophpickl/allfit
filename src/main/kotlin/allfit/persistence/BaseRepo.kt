package allfit.persistence

interface BaseRepo<ENTITY : BaseEntity> {
    fun selectAll(): List<ENTITY>
    fun insertAll(entities: List<ENTITY>)
    fun deleteAll(ids: List<Int>)
}
