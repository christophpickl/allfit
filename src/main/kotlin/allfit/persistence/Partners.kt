package allfit.persistence

import allfit.domain.Partner
import mu.KotlinLogging.logger
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.SizedCollection
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.transactions.transaction

object PartnersTable : IntIdTable("PUBLIC.PARTNERS", "ID") {
    val name = varchar("NAME", 256)
    val isDeleted = bool("IS_DELETED")
    // isHidden

    fun selectByIds(ids: List<EntityID<Int>>): Map<Int, PartnerDbo> {
        return PartnerDbo.find { PartnersTable.id inList ids }.associateBy { it.id.value }
    }
}

fun Map<Int, PartnerDbo>.findOrThrow(id: Int) =
    this[id] ?: throw PartnerNotFoundException("Could not find partner by ID: $id!")

object PartnersCategoriesTable : Table("PUBLIC.PARTNERS_CATEGORIES") {
    val partner = reference("PARTNER", PartnersTable)
    val category = reference("CATEGORY", CategoriesTable)
    override val primaryKey = PrimaryKey(partner, category, name = "PK_PARTNERS_CATEGORIES")
}

class PartnerDbo(id: EntityID<Int>) : IntEntity(id), MutableDeletable {
    companion object : IntEntityClass<PartnerDbo>(PartnersTable)

    var name by PartnersTable.name
    override var isDeleted by PartnersTable.isDeleted
    var categories by CategoryDbo via PartnersCategoriesTable
}

interface PartnersRepo : Repo<Partner>

class PartnerNotFoundException(message: String) : Exception(message)

class InMemoryPartnersRepo : PartnersRepo {

    private val log = logger {}
    private val partners = mutableMapOf<Int, Partner>()

    override fun select() = partners.values.toList()

    override fun insert(domainObjects: List<Partner>) {
        log.debug { "Inserting ${domainObjects.size} partners." }
        domainObjects.forEach {
            partners[it.id] = it
        }
    }

    override fun delete(ids: List<Int>) {
        log.debug { "Deleting ${ids.size} partners." }
        ids.forEach { id ->
            partners[id] = partners[id]!!.copy(isDeleted = true)
        }
    }
}

object ExposedPartnersRepo : PartnersRepo {

    private val log = logger {}

    override fun select() = transaction {
        log.debug { "Loading partners." }
        PartnerDbo.all().map { it.toPartner() }
    }

    override fun insert(domainObjects: List<Partner>) {
        transaction {
            log.debug { "Inserting ${domainObjects.size} partners." }
            val categoryDbosById = CategoriesTable.selectByIds(domainObjects.toDistinctCategoryIds())
            domainObjects.forEach { partner ->
                PartnerDbo.new(partner.id) {
                    name = partner.name
                    isDeleted = partner.isDeleted
                    categories = SizedCollection(partner.categories.map {
                        categoryDbosById.findOrThrow(it.id)
                    })
                }
            }
        }
    }

    override fun delete(ids: List<Int>) {
        markDeleted(PartnersTable, PartnerDbo, ids, "partners")
    }
}

private fun List<Partner>.toDistinctCategoryIds() = map { it.categories.map { it.id } }.flatten().distinct()
    .map { EntityID(it, CategoriesTable) }


fun PartnerDbo.toPartner() = Partner(
    id = id.value,
    name = name,
    isDeleted = isDeleted,
    categories = categories.map { it.toCategory() }
)
