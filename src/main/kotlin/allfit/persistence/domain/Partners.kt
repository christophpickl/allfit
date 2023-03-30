package allfit.persistence.domain

import allfit.persistence.BaseEntity
import allfit.persistence.BaseRepo
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
    val imageUrl = varchar("IMAGE_URL", 256)
    val facilities = text("FACILITIES") // comma separated list
    val isDeleted = bool("IS_DELETED") // custom
    val isFavorited = bool("IS_FAVORITED") // custom
    val isWishlisted = bool("IS_WISHLISTED") // custom
    val isHidden = bool("IS_HIDDEN") // custom
}

object PartnersCategoriesTable : Table("PUBLIC.PARTNERS_CATEGORIES") {
    val partnerId = reference("PARTNER_ID", PartnersTable)
    val categoryId = reference("CATEGORY_ID", CategoriesTable)
    val isPrimary = bool("IS_PRIMARY")
    override val primaryKey = PrimaryKey(partnerId, categoryId, name = "PK_PARTNERS_CATEGORIES")
}

data class PartnerCategoryEntity(
    val partnerId: Int,
    val categoryId: Int,
    val isPrimary: Boolean,
)

data class PartnerEntity(
    override val id: Int,
    val primaryCategoryId: Int,
    val secondaryCategoryIds: List<Int>,
    val name: String,
    val slug: String,
    val description: String,
    val note: String,
    val facilities: String,
    override val isDeleted: Boolean,
    val imageUrl: String,
    val isFavorited: Boolean,
    val isWishlisted: Boolean,
    val isHidden: Boolean,
) : BaseEntity

interface PartnersRepo : BaseRepo<PartnerEntity>

class InMemoryPartnersRepo : PartnersRepo {

    private val log = logger {}
    val partners = mutableMapOf<Int, PartnerEntity>()

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
        val categoriesByPartnerId = selectAllPartnerCategories().groupBy {
            it.partnerId
        }
        PartnersTable.selectAll().map { result ->
            val partnerId = result[PartnersTable.id].value
            val categeoriesForPartner = categoriesByPartnerId[partnerId]
                ?: error("No categories found for partner with ID: $partnerId")
            val primaryCategory = categeoriesForPartner.first { it.isPrimary }
            val secondaryCategories = categeoriesForPartner.filter { !it.isPrimary }
            result.toPartnerEntity(
                primaryCategoryId = primaryCategory.categoryId,
                secondaryCategoryIds = secondaryCategories.map { it.categoryId }
            )
        }
    }

    fun selectAllPartnerCategories(): List<PartnerCategoryEntity> = transaction {
        PartnersCategoriesTable.selectAll().map {
            PartnerCategoryEntity(
                partnerId = it[PartnersCategoriesTable.partnerId].value,
                categoryId = it[PartnersCategoriesTable.categoryId].value,
                isPrimary = it[PartnersCategoriesTable.isPrimary],
            )
        }
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
                    it[imageUrl] = partner.imageUrl
                    it[isWishlisted] = partner.isWishlisted
                    it[isFavorited] = partner.isFavorited
                    it[isHidden] = partner.isHidden
                    it[isDeleted] = partner.isDeleted
                }
                PartnersCategoriesTable.insert {
                    it[partnerId] = partner.id
                    it[categoryId] = partner.primaryCategoryId
                    it[isPrimary] = true
                }
                partner.secondaryCategoryIds.forEach { secondaryCategoryId ->
                    PartnersCategoriesTable.insert {
                        it[partnerId] = partner.id
                        it[categoryId] = secondaryCategoryId
                        it[isPrimary] = false
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

private fun ResultRow.toPartnerEntity(primaryCategoryId: Int, secondaryCategoryIds: List<Int>) = PartnerEntity(
    id = this[PartnersTable.id].value,
    primaryCategoryId = primaryCategoryId,
    secondaryCategoryIds = secondaryCategoryIds,
    name = this[PartnersTable.name],
    slug = this[PartnersTable.slug],
    description = this[PartnersTable.description],
    note = this[PartnersTable.note],
    facilities = this[PartnersTable.facilities],
    imageUrl = this[PartnersTable.imageUrl],
    isDeleted = this[PartnersTable.isDeleted],
    isFavorited = this[PartnersTable.isFavorited],
    isHidden = this[PartnersTable.isHidden],
    isWishlisted = this[PartnersTable.isWishlisted],
)
