package allfit.sync

import allfit.api.models.PartnerJson
import allfit.api.models.PartnersJsonRoot
import allfit.persistence.domain.PartnerEntity
import allfit.persistence.domain.PartnersRepo
import allfit.service.ImageStorage
import allfit.service.PartnerAndImageUrl
import mu.KotlinLogging.logger

interface PartnersSyncer {
    suspend fun sync(partners: PartnersJsonRoot)
}

class PartnersSyncerImpl(
    private val partnersRepo: PartnersRepo,
    private val imageStorage: ImageStorage
) : PartnersSyncer {
    private val log = logger {}

    override suspend fun sync(partners: PartnersJsonRoot) {
        log.debug { "Syncing partners ..." }
        val report = syncAny(partnersRepo, partners.data) {
            it.toPartnerEntity()
        }
        imageStorage.savePartnerImages(report.toInsert.map {
            PartnerAndImageUrl(it.id, it.imageUrl)
        })
    }
}

private fun PartnerJson.toPartnerEntity() = PartnerEntity(
    id = id,
    primaryCategoryId = category.id,
    secondaryCategoryIds = categories.map { it.id }.distinct().minus(category.id), // OneFit sends corrupt data :-/
    name = name,
    slug = slug,
    description = description,
    note = "",
    imageUrl = header_image.orig,
    facilities = facilities.joinToString(","),
    isDeleted = false,
    isFavorited = false,
    isHidden = false,
    isWishlisted = false,
)
