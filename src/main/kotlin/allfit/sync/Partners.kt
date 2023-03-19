package allfit.sync

import allfit.api.models.PartnerJson
import allfit.api.models.PartnersJson
import allfit.persistence.PartnerEntity
import allfit.persistence.PartnersRepo
import allfit.service.ImageStorage
import allfit.service.PartnerAndImageUrl
import mu.KotlinLogging.logger

class PartnersSyncer(
    private val partnersRepo: PartnersRepo,
    private val imageStorage: ImageStorage
) {
    private val log = logger {}

    suspend fun sync(partners: PartnersJson) {
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
    categoryIds = mutableListOf<Int>().apply {
        add(category.id)
        addAll(categories.map { it.id })
    }.distinct(), // OneFit sends corrupt data :-/
    name = name,
    slug = slug,
    description = description,
    note = "",
    imageUrl = header_image.orig,
    facilities = facilities.joinToString(","),
    isDeleted = false,
    isFavorited = false,
    isHidden = false,
    isStarred = false,
)
