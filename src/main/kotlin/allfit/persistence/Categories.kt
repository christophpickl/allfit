package allfit.persistence

import mu.KotlinLogging.logger
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update


interface CategoriesRepo {
    fun load(): List<CategoryDbo>
    fun insert(categories: List<CategoryDbo>)
    fun delete(categoryIds: List<Int>)
}

object InMemoryCategoriesRepo : CategoriesRepo {

    private val log = logger {}
    private val categories = mutableListOf<CategoryDbo>()

    init {
        categories += CategoryDbo(1, "Foo", false)
        categories += CategoryDbo(2, "Bar", false)
    }

    override fun load() = categories

    override fun insert(categories: List<CategoryDbo>) {
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
        CategoriesTable.selectAll().toList().map {
            CategoryDbo(
                id = it[CategoriesTable.id],
                name = it[CategoriesTable.name],
                isDeleted = it[CategoriesTable.isDeleted],
            )
        }
    }

    override fun insert(categories: List<CategoryDbo>) {
        transaction {
            log.debug { "Inserting ${categories.size} categories." }
            categories.forEach { category ->
                CategoriesTable.insert {
                    it[id] = category.id
                    it[name] = category.name
                    it[isDeleted] = false
                }
            }
        }
    }

    override fun delete(categoryIds: List<Int>) {
        transaction {
            log.debug { "Deleting ${categoryIds.size} categories." }
            CategoriesTable.update(where = {
                CategoriesTable.id inList categoryIds
            }) {
                it[isDeleted] = true
            }
        }
    }
}

data class CategoryDbo(
    val id: Int,
    val name: String,
    val isDeleted: Boolean
) {
    init {
        require(name.length < 256)
    }
}

object CategoriesTable : Table("PUBLIC.CATEGORIES") {
    val id = integer("ID")
    val name = varchar("NAME", 256)
    val isDeleted = bool("IS_DELETED")

    override val primaryKey = PrimaryKey(id)
}
