package allfit.sync.domain

import allfit.api.models.PartnerJson
import allfit.api.models.PartnersJsonRoot
import allfit.domain.Location
import allfit.persistence.domain.PartnerEntity
import allfit.persistence.domain.PartnersRepo
import allfit.persistence.domain.SinglesRepo
import allfit.service.ImageStorage
import allfit.service.PartnerAndImageUrl
import allfit.sync.core.SyncListenerManager
import io.github.oshai.kotlinlogging.KotlinLogging.logger

interface PartnersSyncer {
    suspend fun sync(partners: PartnersJsonRoot)
}

class PartnersSyncerImpl(
    private val partnersRepo: PartnersRepo,
    private val imageStorage: ImageStorage,
    private val listeners: SyncListenerManager,
    private val singlesRepo: SinglesRepo,
) : PartnersSyncer {
    private val log = logger {}

    override suspend fun sync(partners: PartnersJsonRoot) {
        log.debug { "Syncing partners ..." }
        val location = singlesRepo.selectLocation()
        val report = syncAny(partnersRepo, partners.data) {
            it.toPartnerEntity(location)
        }
        if (report.toInsert.isEmpty()) {
            listeners.onSyncDetail("No new partners available.")
        } else {
            listeners.onSyncDetailReport(report, "partners")
            listeners.onSyncDetail("Fetching ${report.toInsert.size} partner images.")
            imageStorage.savePartnerImages(report.toInsert.map {
                PartnerAndImageUrl(it.id, it.imageUrl)
            })
        }
    }
}

private fun PartnerJson.toPartnerEntity(location: Location) = PartnerEntity(
    id = id,
    primaryCategoryId = category.id,
    secondaryCategoryIds = categories.map { it.id }.distinct().minus(category.id), // OneFit sends corrupt data :-/
    name = name,
    slug = slug,
    description = description,
    imageUrl = header_image?.orig,
    facilities = facilities.joinToString(","),
    hasDropins = settlement_options.drop_in_enabled,
    hasWorkouts = settlement_options.reservable_workouts,
    // custom fields:
    note = "",
    officialWebsite = null,
    rating = 0,
    isDeleted = false,
    isFavorited = false,
    isHidden = false,
    isWishlisted = false,
    locationShortCode = location.shortCode,
)
