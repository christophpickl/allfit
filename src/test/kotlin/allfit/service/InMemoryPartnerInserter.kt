package allfit.service

import allfit.persistence.domain.PartnerEntity

class InMemoryPartnerInserter : PartnerInserter {

    val inserted = mutableListOf<PartnerEntity>()

    override fun selectAllIds(): List<Int> =
        inserted.map { it.id }

    override suspend fun insertAllWithImage(partnersToInsert: List<PartnerEntity>) {
        inserted += partnersToInsert
    }
}