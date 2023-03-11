package allfit.domain

interface BaseDomain : HasIntId, Deletable

interface HasIntId {
    val id: Int
}

interface Deletable {
    val isDeleted: Boolean
}
