package allfit.persistence.domain

import allfit.persistence.BaseEntity
import allfit.persistence.BaseRepo
import mu.KotlinLogging.logger
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

object CategoriesTable : IntIdTable("PUBLIC.CATEGORIES", "ID") {
    val name = varchar("NAME", 256)

    /** In english. For categories only retrieved from partners, we don't get a slug. */
    val slug = varchar("SLUG", 256).nullable()
    val isDeleted = bool("IS_DELETED")
}

data class CategoryEntity(
    override val id: Int,
    val name: String,
    override val isDeleted: Boolean,
    val slug: String?,
) : BaseEntity

interface CategoriesRepo : BaseRepo<CategoryEntity>

class InMemoryCategoriesRepo : CategoriesRepo {

    private val log = logger {}
    val categories = mutableMapOf<Int, CategoryEntity>()

    override fun selectAll() = categories.values.toList()

    override fun insertAll(entities: List<CategoryEntity>) {
        log.debug { "Inserting ${entities.size} categories." }
        entities.forEach {
            categories[it.id] = it
        }
    }

    override fun deleteAll(ids: List<Int>) {
        log.debug { "Deleting ${ids.size} categories." }
        ids.forEach { id ->
            categories[id] = categories[id]!!.copy(isDeleted = true)
        }
    }
}

object ExposedCategoriesRepo : CategoriesRepo {

    private val log = logger {}

    override fun selectAll() = transaction {
        log.debug { "Loading categories." }
        CategoriesTable.selectAll().map { it.toCategoryEntity() }
    }

    override fun insertAll(entities: List<CategoryEntity>) {
        transaction {
            log.debug { "Inserting ${entities.size} categories." }
            entities.forEach { category ->
                CategoriesTable.insert {
                    it[id] = category.id
                    it[isDeleted] = category.isDeleted
                    it[name] = category.name
                    it[slug] = category.slug
                }
            }
        }
    }

    override fun deleteAll(ids: List<Int>) {
        require(ids.isNotEmpty())
        transaction {
            log.debug { "Deleting ${ids.size} categories." }
            CategoriesTable.update(where = {
                CategoriesTable.id inList ids
            }) {
                it[isDeleted] = true
            }
        }
    }
}

private fun ResultRow.toCategoryEntity() = CategoryEntity(
    id = this[CategoriesTable.id].value,
    isDeleted = this[CategoriesTable.isDeleted],
    name = this[CategoriesTable.name],
    slug = this[CategoriesTable.slug],
)
