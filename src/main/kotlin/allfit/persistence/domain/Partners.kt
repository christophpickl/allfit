package allfit.persistence.domain

import allfit.persistence.BaseEntity
import allfit.persistence.BaseRepo
import allfit.presentation.models.PartnerCustomAttributesRead
import allfit.presentation.partners.PartnerModifications
import io.github.oshai.kotlinlogging.KotlinLogging.logger
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

object PartnersTable : IntIdTable("PUBLIC.PARTNERS", "ID") {
    val name = varchar("NAME", 256)
    val slug = varchar("SLUG", 256)
    val description = text("DESCRIPTION")
    val note = text("NOTE") // custom
    val locationShortCode = varchar("LOCATION", 3)
    val imageUrl = varchar("IMAGE_URL", 256).nullable()
    val officialWebsite = varchar("OFFICIAL_WEBSITE", 256).nullable()
    val facilities = text("FACILITIES") // comma separated list
    val isDeleted = bool("IS_DELETED") // custom
    val rating = integer("RATING") // custom
    val hasDropins = bool("HAS_DROPINS").nullable() // settlement_options.drop_in_enabled
    val hasWorkouts = bool("HAS_WORKOUTS").nullable() // settlement_options.reservable_workouts
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
    val facilities: String,
    override val rating: Int,
    override val isDeleted: Boolean,
    val imageUrl: String?,
    override val note: String,
    override val officialWebsite: String?,
    override val isFavorited: Boolean,
    override val isWishlisted: Boolean,
    val isHidden: Boolean,
    val hasDropins: Boolean?,
    val hasWorkouts: Boolean?,
    val locationShortCode: String,
) : BaseEntity, PartnerCustomAttributesRead

interface PartnersRepo : BaseRepo<PartnerEntity> {
    fun selectAllIds(): List<Int>
    fun update(modifications: PartnerModifications)
    fun hide(partnerId: Int)
    fun unhide(partnerId: Int)
}

class InMemoryPartnersRepo : PartnersRepo {

    private val log = logger {}
    val partners = mutableMapOf<Int, PartnerEntity>()

    override fun selectAllIds(): List<Int> =
        partners.keys.toList()

    override fun update(modifications: PartnerModifications) {
        val old = partners[modifications.partnerId]!!
        val new = modifications.modify(old)
        partners[new.id] = new
    }

    override fun hide(partnerId: Int) {
        val partner = partners[partnerId]!!
        partners[partnerId] = partner.copy(isHidden = true)
    }

    override fun unhide(partnerId: Int) {
        val partner = partners[partnerId]!!
        partners[partnerId] = partner.copy(isHidden = false)
    }

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
        }.also {
            log.debug { "Selecting all returns ${it.size} partners." }
        }
    }

    override fun selectAllIds(): List<Int> = transaction {
        PartnersTable.slice(PartnersTable.id).selectAll().map { it[PartnersTable.id].value }
    }

    fun selectAllPartnerCategories(): List<PartnerCategoryEntity> = transaction {
        PartnersCategoriesTable.selectAll().map {
            it.toPartnerCategoryEntity()
        }
    }

    private fun selectPartnerCategoriesFor(partnerId: Int): List<PartnerCategoryEntity> = transaction {
        PartnersCategoriesTable.select { PartnersCategoriesTable.partnerId eq partnerId }
            .map { it.toPartnerCategoryEntity() }
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
                    it[locationShortCode] = partner.locationShortCode
                    it[rating] = partner.rating
                    it[facilities] = partner.facilities
                    it[officialWebsite] = partner.officialWebsite
                    it[imageUrl] = partner.imageUrl
                    it[isWishlisted] = partner.isWishlisted
                    it[isFavorited] = partner.isFavorited
                    it[hasDropins] = partner.hasDropins
                    it[hasWorkouts] = partner.hasWorkouts
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

    override fun update(modifications: PartnerModifications): Unit = transaction {
        log.info { "Update: $modifications" }
        val old = selectPartnerAndCategoriesById(modifications.partnerId)
        val new = modifications.modify(old)
        PartnersTable.update(where = {
            PartnersTable.id eq new.id
        }) {
            modifications.prepare(new, it)
        }
    }

    private fun selectPartnerAndCategoriesById(partnerId: Int): PartnerEntity {
        val list = PartnersTable.select { PartnersTable.id eq partnerId }.toList()
        require(list.size == 1) { "Expected 1 but got ${list.size} partners for ID: ${partnerId}" }
        val categories = selectPartnerCategoriesFor(partnerId)
        val primaryCategoryId = categories.first { it.isPrimary }.categoryId
        val secondaryCategoryIds = categories.filter { !it.isPrimary }.map { it.categoryId }
        return list.first().toPartnerEntity(
            primaryCategoryId = primaryCategoryId,
            secondaryCategoryIds = secondaryCategoryIds,
        )
    }

    override fun hide(partnerId: Int) {
        updateHiddenField(partnerId, true)
    }

    override fun unhide(partnerId: Int) {
        updateHiddenField(partnerId, false)
    }

    private fun updateHiddenField(partnerId: Int, newHiddenValue: Boolean): Unit = transaction {
        log.info { "Hide partner with ID: $partnerId = $newHiddenValue" }
        PartnersTable.update(where = {
            PartnersTable.id eq partnerId
        }) {
            it[isHidden] = newHiddenValue
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
    rating = this[PartnersTable.rating],
    facilities = this[PartnersTable.facilities],
    imageUrl = this[PartnersTable.imageUrl],
    isDeleted = this[PartnersTable.isDeleted],
    isFavorited = this[PartnersTable.isFavorited],
    isHidden = this[PartnersTable.isHidden],
    isWishlisted = this[PartnersTable.isWishlisted],
    locationShortCode = this[PartnersTable.locationShortCode],
    hasDropins = this[PartnersTable.hasDropins],
    hasWorkouts = this[PartnersTable.hasWorkouts],
    officialWebsite = this[PartnersTable.officialWebsite],
)

private fun ResultRow.toPartnerCategoryEntity() = PartnerCategoryEntity(
    partnerId = this[PartnersCategoriesTable.partnerId].value,
    categoryId = this[PartnersCategoriesTable.categoryId].value,
    isPrimary = this[PartnersCategoriesTable.isPrimary],
)

