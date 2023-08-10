package allfit.persistence

import allfit.persistence.domain.CategoriesTable
import allfit.persistence.domain.CheckinsTable
import allfit.persistence.domain.LocationsTable
import allfit.persistence.domain.PartnersCategoriesTable
import allfit.persistence.domain.PartnersTable
import allfit.persistence.domain.ReservationsTable
import allfit.persistence.domain.SinglesTable
import allfit.persistence.domain.UsageTable
import allfit.persistence.domain.WorkoutsTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import org.jetbrains.exposed.sql.update

val allTables = arrayOf(
    CategoriesTable,
    PartnersTable,
    PartnersCategoriesTable,
    WorkoutsTable,
    ReservationsTable,
    LocationsTable,
    CheckinsTable,
    UsageTable,
    SinglesTable
)

interface HasIntId {
    val id: Int
}

interface BaseEntity : HasIntId, Deletable

interface Deletable {
    val isDeleted: Boolean
}

fun Table.selectSingleton(): ResultRow {
    val list = selectAll().toList()
    return when (list.size) {
        0 -> error("No entity existing yet for table '$tableName'!")
        1 -> list.first()
        else -> error("Expected to be exactly one entity in table '$tableName' but there were: ${list.size}")
    }
}

fun <T : Table> T.upsert(body: T.(UpdateBuilder<Int>) -> Unit) {
    val count = selectAll().count()
    if (count == 0L) {
        insert(body = body)
    } else {
        update(body = body)
    }
}
