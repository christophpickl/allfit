package allfit.persistence

import allfit.domain.Categories
import allfit.domain.Category
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

interface CategoriesRepo {
    fun load(): Categories
    fun save(categories: List<Category>)
}

object InMemoryCategoriesRepo : CategoriesRepo {
    private val categories = mutableListOf<Category>()

    override fun load() = Categories(categories)

    override fun save(categories: List<Category>) {
        this.categories += categories
    }
}

object ExposedCategoriesRepo : CategoriesRepo {

    override fun load() = transaction {
        Categories(CategoriesTable.selectAll().toList().map {
            Category(
                id = it[CategoriesTable.id],
                shortCode = it[CategoriesTable.shortCode],
            )
        })
    }

    override fun save(categories: List<Category>) {
        transaction {
            categories.forEach { category ->
                CategoriesTable.insert {
                    it[id] = category.id
                    it[shortCode] = category.shortCode
                }
            }
        }
    }
}

object CategoriesTable : Table("PUBLIC.CATEGORIES") {
    val id = integer("ID")
    val shortCode = varchar("SHORTCODE", 64)

    override val primaryKey = PrimaryKey(id)
}
