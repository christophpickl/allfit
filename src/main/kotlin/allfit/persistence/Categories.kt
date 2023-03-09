package allfit.persistence

import allfit.domain.Categories
import allfit.domain.Category
import mu.KotlinLogging.logger
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

interface CategoriesRepo {
    fun load(): Categories
    fun insert(categories: List<Category>)
    fun delete(categoryIds: List<Int>)
}

object InMemoryCategoriesRepo : CategoriesRepo {

    private val log = logger {}
    private val categories = mutableListOf<Category>()

    init {
        categories += Category(1, "Foo")
        categories += Category(2, "Bar")
    }

    override fun load() = Categories(categories)

    override fun insert(categories: List<Category>) {
        log.debug { "Inserting ${categories.size} categories." }
        this.categories += categories
    }

    override fun delete(categoryIds: List<Int>) {
        log.debug { "Deleting ${categoryIds.size} categories." }
        categories.removeIf { categoryIds.contains(it.id) }
    }
}

object ExposedCategoriesRepo : CategoriesRepo {

    private val log = logger {}

    override fun load() = transaction {
        log.debug { "Loading categories." }
        Categories(CategoriesTable.selectAll().toList().map {
            Category(
                id = it[CategoriesTable.id],
                name = it[CategoriesTable.name],
            )
        })
    }

    override fun insert(categories: List<Category>) {
        transaction {
            log.debug { "Inserting ${categories.size} categories." }
            categories.forEach { category ->
                CategoriesTable.insert {
                    it[id] = category.id
                    it[name] = category.name
                }
            }
        }
    }

    override fun delete(categoryIds: List<Int>) {
        transaction {
            log.debug { "Deleting ${categoryIds.size} categories." }
            // TODO or simply mark as deleted, to retain foreign reference?
            CategoriesTable.deleteWhere {
                id inList categoryIds
            }
        }
    }
}

object CategoriesTable : Table("PUBLIC.CATEGORIES") {
    val id = integer("ID")
    val name = varchar("NAME", 256)

    override val primaryKey = PrimaryKey(id)
}
