package allfit.persistence.domain

import allfit.domain.Location
import allfit.persistence.selectSingleton
import io.github.oshai.kotlinlogging.KotlinLogging.logger
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

private const val SYNC_DAYS_DEFAULT = 7

data class PreferencesData(
    val location: Location,
    val syncDays: Int,
)

interface SinglesRepo {
    fun selectNotes(): String
    fun updateNotes(notes: String)
    fun selectPreferencesData(): PreferencesData
    fun updatePreferencesData(prefs: PreferencesData)
    fun selectLocation(): Location
    fun updateLocation(location: Location)
}

object SinglesTable : Table("PUBLIC.SINGLES") {
    val notes = largeText("NOTES")
    val location = char("LOCATION", 3) // its shortcode
    val syncDays = integer("SYNC_DAYS")
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

    override fun selectPreferencesData() = transaction {
        ensureDefault()
        val result = SinglesTable.selectSingleton()
        PreferencesData(
            location = Location.byShortCode(result[SinglesTable.location]),
            syncDays = result[SinglesTable.syncDays],
        )
    }

    override fun updatePreferencesData(prefs: PreferencesData): Unit = transaction {
        log.debug { "updatePreferencesData($prefs)" }
        ensureDefault()
        SinglesTable.update {
            it[location] = prefs.location.shortCode
            it[syncDays] = prefs.syncDays
        }
    }

    override fun selectLocation() = selectPreferencesData().location
    override fun updateLocation(location: Location) {
        updatePreferencesData(selectPreferencesData().copy(location = location))
    }

    private fun ensureDefault() {
        val count = SinglesTable.selectAll().count()
        if (count == 0L) {
            SinglesTable.insert {
                it[notes] = ""
                it[location] = Location.DEFAULT.shortCode
                it[syncDays] = SYNC_DAYS_DEFAULT
            }
        }
    }
}

class InMemorySinglesRepo : SinglesRepo {

    private val log = logger {}
    private var notes = ""
    private var location = Location.Amsterdam
    private var syncDays = SYNC_DAYS_DEFAULT

    override fun selectNotes() = notes

    override fun updateNotes(notes: String) {
        log.debug { "updateNotes(...)" }
        this.notes = notes
    }

    override fun selectPreferencesData() = PreferencesData(
        location = location, syncDays = syncDays,

        )

    override fun updatePreferencesData(prefs: PreferencesData) {
        log.debug { "updatePreferencesData($prefs)" }
        location = prefs.location
        syncDays = prefs.syncDays
    }

    override fun selectLocation() = location
    override fun updateLocation(location: Location) {
        this.location = location
    }
}
