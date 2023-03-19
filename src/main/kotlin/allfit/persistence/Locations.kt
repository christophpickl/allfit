package allfit.persistence

import mu.KotlinLogging.logger
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

object LocationsTable : IntIdTable("PUBLIC.LOCATIONS", "ID") {
    val partnerId = reference("PARTNER_ID", PartnersTable)
    val streetName = varchar("STREET_NAME", 256)
    val houseNumber = varchar("HOUSE_NUMBER", 256)
    val addition = varchar("ADDITION", 256)
    val zipCode = varchar("ZIP_CODE", 256)
    val city = varchar("CITY", 256)
    val latitude = double("LATITUDE")
    val longitude = double("LONGITUDE")
}

data class LocationEntity(
    val id: Int,
    val partnerId: Int,
    val streetName: String,
    val houseNumber: String,
    val addition: String,
    val zipCode: String,
    val city: String,
    val latitude: Double,
    val longitude: Double,
)

interface LocationsRepo {
    fun selectAll(): List<LocationEntity>
    fun insertAllIfNotYetExists(locations: List<LocationEntity>)
}

class InMemoryLocationsRepo : LocationsRepo {
    private val locations = mutableMapOf<Int, LocationEntity>()

    override fun selectAll(): List<LocationEntity> =
        locations.values.toList()

    override fun insertAllIfNotYetExists(locations: List<LocationEntity>) {
        locations.forEach {
            this.locations[it.id] = it
        }
    }
}

object ExposedLocationsRepo : LocationsRepo {

    private val log = logger {}

    override fun selectAll(): List<LocationEntity> = transaction {
        LocationsTable.selectAll().map { it.toLocationEntity() }
    }

    override fun insertAllIfNotYetExists(locations: List<LocationEntity>) {
        transaction {
            log.debug { "Inserting ${locations.size} locations." }
            locations.forEach { location ->
                val notExisting = LocationsTable.select {
                    LocationsTable.id eq location.id
                }.empty()
                if (notExisting) {
                    LocationsTable.insert {
                        it[id] = EntityID(location.id, LocationsTable)
                        it[partnerId] = EntityID(location.partnerId, PartnersTable)
                        it[streetName] = location.streetName
                        it[houseNumber] = location.houseNumber
                        it[addition] = location.addition
                        it[zipCode] = location.zipCode
                        it[city] = location.city
                        it[latitude] = location.latitude
                        it[longitude] = location.longitude
                    }
                } else {
                    log.warn { "Skipping already existing location: $location" }
                }
            }
        }
    }

    private fun ResultRow.toLocationEntity() = LocationEntity(
        id = this[LocationsTable.id].value,
        partnerId = this[LocationsTable.partnerId].value,
        streetName = this[LocationsTable.streetName],
        houseNumber = this[LocationsTable.houseNumber],
        addition = this[LocationsTable.addition],
        zipCode = this[LocationsTable.zipCode],
        city = this[LocationsTable.city],
        latitude = this[LocationsTable.latitude],
        longitude = this[LocationsTable.longitude],
    )
}
