package allfit.sync.domain

import allfit.api.models.PartnerLocationJson
import allfit.api.models.PartnersJsonRoot
import allfit.persistence.domain.LocationEntity
import allfit.persistence.domain.LocationsRepo
import mu.KotlinLogging.logger

interface LocationsSyncer {
    fun sync(partners: PartnersJsonRoot)
}

class LocationsSyncerImpl(
    private val locationsRepo: LocationsRepo,
) : LocationsSyncer {
    private val log = logger {}

    override fun sync(partners: PartnersJsonRoot) {
        log.debug { "Syncing locations..." }
        val locations = partners.data.map { partner ->
            partner.location_groups.map { it.locations }.flatten()
        }.flatten()
        locationsRepo.insertAllIfNotYetExists(locations.map { it.toLocationEntity() })
    }
}

private fun PartnerLocationJson.toLocationEntity() = LocationEntity(
    id = id.toIntOrNull() ?: error("Invalid, non-numeric location ID '$id'!"),
    partnerId = partner_id,
    streetName = street_name,
    houseNumber = house_number,
    addition = addition,
    zipCode = zip_code,
    city = city,
    latitude = latitude,
    longitude = longitude,
)
