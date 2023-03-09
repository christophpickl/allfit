package allfit.persistence

interface Repo<DBO> {
    fun select(): List<DBO>
    fun insert(dbos: List<DBO>)
    fun delete(ids: List<Int>)
}
