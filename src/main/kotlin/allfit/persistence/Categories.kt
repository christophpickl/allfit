package allfit.persistence

import mu.KotlinLogging.logger
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

interface CategoriesRepo : Repo<CategoryDbo>

data class CategoryDbo(
    override val id: Int,
    override val isDeleted: Boolean,
    val name: String,
) : Dbo {
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

class InMemoryCategoriesRepo : CategoriesRepo {

    private val log = logger {}
    private val categories = mutableListOf<CategoryDbo>()

    override fun select() = categories

    override fun insert(dbos: List<CategoryDbo>) {
        log.debug { "Inserting ${dbos.size} categories." }
        categories += dbos
    }

    override fun delete(ids: List<Int>) {
        log.debug { "Deleting ${ids.size} categories." }

        val toDelete = categories.filter { ids.contains(it.id) }
        categories.removeAll(toDelete)
        categories.addAll(toDelete.map {
            it.copy(isDeleted = true)
        })
    }
}

object ExposedCategoriesRepo : CategoriesRepo {

    private val log = logger {}

    override fun select() = transaction {
        log.debug { "Loading categories." }
        CategoriesTable.selectAll().toList().map {
            CategoryDbo(
                id = it[CategoriesTable.id],
                name = it[CategoriesTable.name],
                isDeleted = it[CategoriesTable.isDeleted],
            )
        }
    }

    override fun insert(dbos: List<CategoryDbo>) {
        transaction {
            log.debug { "Inserting ${dbos.size} categories." }
            dbos.forEach { category ->
                CategoriesTable.insert {
                    it[id] = category.id
                    it[name] = category.name
                    it[isDeleted] = category.isDeleted
                }
            }
        }
    }

    override fun delete(ids: List<Int>) {
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
