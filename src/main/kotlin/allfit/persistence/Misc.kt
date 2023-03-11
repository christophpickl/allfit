package allfit.persistence

import allfit.domain.Deletable
import mu.KotlinLogging.logger
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.transactions.transaction

private val log = logger {}

val allTables = arrayOf(
    CategoriesTable,
    PartnersTable,
    PartnersCategoriesTable,
)

interface MutableDeletable : Deletable {
    override var isDeleted: Boolean
}

fun <ENTITY> markDeleted(
    table: IntIdTable,
    dbo: IntEntityClass<ENTITY>,
    ids: List<Int>,
    entityName: String
) where ENTITY : IntEntity, ENTITY : MutableDeletable {
    require(ids.isNotEmpty())
    transaction {
        log.debug { "Deleting ${ids.size} $entityName." }
        val idEntities = ids.map { EntityID(it, table) }
        dbo.find { table.id inList idEntities }.forUpdate().forEach {
            it.isDeleted = true
        }
    }
}
