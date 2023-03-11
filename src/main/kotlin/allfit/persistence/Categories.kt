package allfit.persistence

import allfit.domain.Category
import mu.KotlinLogging.logger
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.transactions.transaction

object CategoriesTable : IntIdTable("PUBLIC.CATEGORIES", "ID") {
    val name = varchar("NAME", 256)
    val isDeleted = bool("IS_DELETED")
}

class CategoryDbo(id: EntityID<Int>) : IntEntity(id), MutableDeletable {
    companion object : IntEntityClass<CategoryDbo>(CategoriesTable)

    var name by CategoriesTable.name
    override var isDeleted by CategoriesTable.isDeleted
}

interface CategoriesRepo : Repo<Category>

class InMemoryCategoriesRepo : CategoriesRepo {

    private val log = logger {}
    private val categories = mutableMapOf<Int, Category>()

    override fun select() = categories.values.toList()

    override fun insert(domainObjects: List<Category>) {
        log.debug { "Inserting ${domainObjects.size} categories." }
        domainObjects.forEach {
            categories[it.id] = it
        }
    }

    override fun delete(ids: List<Int>) {
        log.debug { "Deleting ${ids.size} categories." }
        ids.forEach { id ->
            categories[id] = categories[id]!!.copy(isDeleted = true)
        }
    }
}

object ExposedCategoriesRepo : CategoriesRepo {

    private val log = logger {}

    override fun select() = transaction {
        log.debug { "Loading categories." }
        CategoryDbo.all().map { it.toCategory() }
    }

    override fun insert(domainObjects: List<Category>) {
        transaction {
            log.debug { "Inserting ${domainObjects.size} categories." }
            domainObjects.forEach { category ->
                CategoryDbo.new(category.id) {
                    name = category.name
                    isDeleted = category.isDeleted
                }
            }
        }
    }

    override fun delete(ids: List<Int>) {
        markDeleted(CategoriesTable, CategoryDbo, ids, "categories")
    }
}


fun CategoryDbo.toCategory() = Category(
    id = id.value,
    name = name,
    isDeleted = isDeleted,
)