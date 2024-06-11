package allfit.service

import allfit.persistence.domain.CategoriesRepo
import allfit.persistence.domain.PartnerEntity
import allfit.persistence.domain.PartnersRepo
import io.github.oshai.kotlinlogging.KotlinLogging.logger

interface PartnerInserter  {
    fun selectAllIds(): List<Int>
    suspend fun insertAllWithImage(partnersToInsert: List<PartnerEntity>)
}

class PartnerInserterImpl(
    private val partnersRepo: PartnersRepo,
    private val imageStorage: ImageStorage,
) : PartnerInserter {

    private val log = logger {}

    override fun selectAllIds() = partnersRepo.selectAllIds()

    override suspend fun insertAllWithImage(partnersToInsert: List<PartnerEntity>) {
        if(partnersToInsert.isNotEmpty()) {
            log.debug { "Inserting ${partnersToInsert.size} partners." }
            partnersRepo.insertAll(partnersToInsert)
            imageStorage.savePartnerImages(partnersToInsert.map { it.toPartnerAndImageUrl() })
        }
    }
}
