package allfit.persistence.domain

import allfit.domain.Location
import allfit.persistence.selectSingleton
import io.github.oshai.kotlinlogging.KotlinLogging.logger
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

interface SinglesRepo {
    fun selectNotes(): String
    fun updateNotes(notes: String)
    fun selectLocation(): Location
    fun updateLocation(location: Location)
}

object SinglesTable : Table("PUBLIC.SINGLES") {
    val notes = largeText("NOTES")
    val location = char("LOCATION", 3) // its shortcode
}

object ExposedSinglesRepo : SinglesRepo {

    private val log = logger {}

    override fun selectNotes() = transaction {
        ensureDefault()
        SinglesTable.selectSingleton()[SinglesTable.notes]
    }

    override fun updateNotes(notes: String): Unit = transaction {
        log.debug { "updateNotes(...)" }
        ensureDefault()
        SinglesTable.update {
            it[SinglesTable.notes] = notes
        }
    }

    override fun selectLocation(): Location = transaction {
        ensureDefault()
        Location.byShortCode(SinglesTable.selectSingleton()[SinglesTable.location])
    }

    override fun updateLocation(location: Location): Unit = transaction {
        log.debug { "updateLocation($location)" }
        ensureDefault()
        SinglesTable.update {
            it[SinglesTable.location] = location.shortCode
        }
    }

    private fun ensureDefault() {
        val count = SinglesTable.selectAll().count()
        if (count == 0L) {
            SinglesTable.insert {
                it[notes] = ""
                it[location] = Location.DEFAULT.shortCode
            }
        }
    }
}

class InMemorySinglesRepo : SinglesRepo {

    private var notes = ""
    private var location = Location.Amsterdam

    override fun selectNotes() = notes

    override fun updateNotes(notes: String) {
        this.notes = notes
    }

    override fun selectLocation() = location

    override fun updateLocation(location: Location) {
        this.location = location
    }
}
