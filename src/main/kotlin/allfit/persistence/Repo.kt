package allfit.persistence

import allfit.domain.BaseDomain

interface Repo<DOMAIN : BaseDomain> {
    fun select(): List<DOMAIN>
    fun insert(domainObjects: List<DOMAIN>)
    fun delete(ids: List<Int>)
}
