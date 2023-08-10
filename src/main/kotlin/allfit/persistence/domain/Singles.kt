package allfit.persistence.domain

import allfit.persistence.selectSingleton
import allfit.persistence.upsert
import io.github.oshai.kotlinlogging.KotlinLogging.logger
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

interface SinglesRepo {
    fun selectNotes(): String
    fun updateNotes(notes: String)
}

object SinglesTable : Table("PUBLIC.SINGLES") {
    val notes = largeText("NOTES")
}

object ExposedSinglesRepo : SinglesRepo {

    private val log = logger {}

    override fun selectNotes() = transaction {
        if (SinglesTable.selectAll().count() == 0L) {
            updateNotes("")
        }
        SinglesTable.selectSingleton()[SinglesTable.notes]
    }

    override fun updateNotes(notes: String) = transaction {
        log.debug { "updateNotes(...)" }
        SinglesTable.upsert {
            it[SinglesTable.notes] = notes
        }
    }
}

class InMemorySinglesRepo : SinglesRepo {

    private var notes = ""

    override fun selectNotes() = notes

    override fun updateNotes(notes: String) {
        this.notes = notes
    }
}
