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
    private val imageStorage: ImageStorage,
    private val listeners: SyncListenerManager,
) : PartnersSyncer {
    private val log = logger {}

    override suspend fun sync(partners: PartnersJsonRoot) {
        log.debug { "Syncing partners ..." }
        val report = syncAny(partnersRepo, partners.data) {
            it.toPartnerEntity()
        }
        listeners.onSyncDetail("Fetching ${report.toInsert.size} partner images.")
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
    imageUrl = header_image?.orig,
    facilities = facilities.joinToString(","),
    // custom fields:
    note = "",
    rating = 0,
    isDeleted = false,
    isFavorited = false,
    isHidden = false,
    isWishlisted = false,
)
