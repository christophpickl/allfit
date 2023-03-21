package allfit.sync

import allfit.api.models.PartnerLocationJson
import allfit.api.models.PartnersJson
import allfit.persistence.LocationEntity
import allfit.persistence.LocationsRepo
import mu.KotlinLogging.logger

interface LocationsSyncer {
    fun sync(partners: PartnersJson)
}

class LocationsSyncerImpl(
    private val locationsRepo: LocationsRepo,
) : LocationsSyncer {
    private val log = logger {}

    override fun sync(partners: PartnersJson) {
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
