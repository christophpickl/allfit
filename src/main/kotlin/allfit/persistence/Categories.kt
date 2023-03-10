package allfit.persistence

import mu.KotlinLogging.logger
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

object CategoriesTable : IntIdTable("PUBLIC.CATEGORIES", "ID") {
    val name = varchar("NAME", 256)
    val isDeleted = bool("IS_DELETED")
}

data class CategoryEntity(
    override val id: Int,
    val name: String,
    override val isDeleted: Boolean,
) : BaseEntity

interface CategoriesRepo : BaseRepo<CategoryEntity>

class InMemoryCategoriesRepo : CategoriesRepo {

    private val log = logger {}
    private val categories = mutableMapOf<Int, CategoryEntity>()

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
                    it[CategoriesTable.id] = EntityID(category.id, CategoriesTable)
                    it[isDeleted] = category.isDeleted
                    it[name] = category.name
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
)
