package allfit.sync

import allfit.api.models.PartnerLocationJson
import allfit.api.models.partnerJson
import allfit.api.models.partnerLocationGroupsJson
import allfit.api.models.partnerLocationJson
import allfit.api.models.partnersJson
import allfit.persistence.InMemoryLocationsRepo
import allfit.persistence.LocationEntity
import io.kotest.core.spec.style.StringSpec
import io.kotest.core.test.TestCase
import io.kotest.matchers.collections.shouldBeSingleton
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next

class LocationsSyncerTest : StringSpec() {

    private val location = Arb.partnerLocationJson().next()
    private lateinit var syncer: LocationsSyncer
    private lateinit var locationsRepo: InMemoryLocationsRepo

    override suspend fun beforeEach(testCase: TestCase) {
        locationsRepo = InMemoryLocationsRepo()
        syncer = LocationsSyncerImpl(locationsRepo)
    }

    init {
        "When partner has location Then insert it" {
            syncer.sync(partnersJson(partnerWithLocations(location)))

            locationsRepo.selectAll().shouldBeSingleton().first() shouldBe LocationEntity(
                id = location.id.toInt(),
                partnerId = location.partner_id,
                streetName = location.street_name,
                houseNumber = location.house_number,
                addition = location.addition,
                zipCode = location.zip_code,
                city = location.city,
                latitude = location.latitude,
                longitude = location.longitude,
            )
        }
    }

    private fun partnerWithLocations(vararg locations: PartnerLocationJson) =
        Arb.partnerJson().next().copy(
            location_groups = listOf(
                Arb.partnerLocationGroupsJson().next().copy(locations = locations.toList())
            )
        )
}
