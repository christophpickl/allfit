package allfit.persistence

import mu.KotlinLogging.logger
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

object PartnersTable : IntIdTable("PUBLIC.PARTNERS", "ID") {
    val name = varchar("NAME", 256)
    val slug = varchar("SLUG", 256)
    val description = text("DESCRIPTION")
    val note = text("NOTE") // custom
    val facilities = text("FACILITIES") // comma separated list
    val isDeleted = bool("IS_DELETED") // custom
    val isFavorited = bool("IS_FAVORITED") // custom
    val isStarred = bool("IS_STARRED") // custom
    val isHidden = bool("IS_HIDDEN") // custom
}

object PartnersCategoriesTable : Table("PUBLIC.PARTNERS_CATEGORIES") {
    val partnerId = reference("PARTNER_ID", PartnersTable)
    val categoryId = reference("CATEGORY_ID", CategoriesTable)
    override val primaryKey = PrimaryKey(partnerId, categoryId, name = "PK_PARTNERS_CATEGORIES")

    fun selectAllCategoryIdsByPartnerIds(): Map<Int, List<Int>> {
        val categoryIdsByPartnerIds = mutableMapOf<Int, MutableList<Int>>()
        PartnersCategoriesTable.selectAll().map {
            it[partnerId].value to it[categoryId].value
        }.groupByTo(categoryIdsByPartnerIds, { it.first }, { it.second })
        return categoryIdsByPartnerIds
    }
}

data class PartnerEntity(
    override val id: Int,
    val categoryIds: List<Int>,
    val name: String,
    val slug: String,
    val description: String,
    val note: String,
    val facilities: String,
    override val isDeleted: Boolean,
    val isFavorited: Boolean,
    val isStarred: Boolean,
    val isHidden: Boolean,
) : BaseEntity

interface PartnersRepo : BaseRepo<PartnerEntity>

class InMemoryPartnersRepo : PartnersRepo {

    private val log = logger {}
    private val partners = mutableMapOf<Int, PartnerEntity>()

    override fun selectAll() = partners.values.toList()

    override fun insertAll(entities: List<PartnerEntity>) {
        log.debug { "Inserting ${entities.size} partners." }
        entities.forEach {
            partners[it.id] = it
        }
    }

    override fun deleteAll(ids: List<Int>) {
        log.debug { "Deleting ${ids.size} partners." }
        ids.forEach { id ->
            partners[id] = partners[id]!!.copy(isDeleted = true)
        }
    }
}

@Suppress("DuplicatedCode")
object ExposedPartnersRepo : PartnersRepo {

    private val log = logger {}

    override fun selectAll() = transaction {
        log.debug { "Loading partners." }
        val categories = PartnersCategoriesTable.selectAllCategoryIdsByPartnerIds()
        PartnersTable.selectAll().map { it.toPartnerEntity(categories[it[PartnersTable.id].value]!!) }
    }

    override fun insertAll(entities: List<PartnerEntity>) {
        transaction {
            log.debug { "Inserting ${entities.size} partners." }
            entities.forEach { partner ->
                PartnersTable.insert {
                    it[PartnersTable.id] = EntityID(partner.id, PartnersTable)
                    it[name] = partner.name
                    it[slug] = partner.slug
                    it[description] = partner.description
                    it[note] = partner.note
                    it[facilities] = partner.facilities
                    it[isStarred] = partner.isStarred
                    it[isFavorited] = partner.isFavorited
                    it[isHidden] = partner.isHidden
                    it[isDeleted] = partner.isDeleted
                }
                partner.categoryIds.forEach { categoryId ->
                    PartnersCategoriesTable.insert {
                        it[partnerId] = partner.id
                        it[this.categoryId] = categoryId
                    }
                }
            }
        }
    }

    override fun deleteAll(ids: List<Int>) {
        require(ids.isNotEmpty())
        transaction {
            log.debug { "Deleting ${ids.size} partners." }
            PartnersTable.update(where = {
                PartnersTable.id inList ids
            }) {
                it[isDeleted] = true
            }
        }
    }
}

private fun ResultRow.toPartnerEntity(categoryIds: List<Int>) = PartnerEntity(
    id = this[PartnersTable.id].value,
    categoryIds = categoryIds,
    name = this[PartnersTable.name],
    slug = this[PartnersTable.slug],
    description = this[PartnersTable.description],
    note = this[PartnersTable.note],
    facilities = this[PartnersTable.facilities],
    isDeleted = this[PartnersTable.isDeleted],
    isFavorited = this[PartnersTable.isFavorited],
    isHidden = this[PartnersTable.isHidden],
    isStarred = this[PartnersTable.isStarred],
)
