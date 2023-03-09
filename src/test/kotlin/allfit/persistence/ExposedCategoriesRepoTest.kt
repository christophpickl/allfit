package allfit.persistence

import io.kotest.core.spec.style.StringSpec
import io.kotest.core.test.TestCase
import io.kotest.core.test.TestResult
import io.kotest.matchers.collections.shouldBeSingleton
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction

class ExposedCategoriesRepoTest : StringSpec() {

    private lateinit var db: Database
    private val category = Arb.categoryDbo().next()
    private val category1 = Arb.categoryDbo().next().copy(id = 1)
    private val category2 = Arb.categoryDbo().next().copy(id = 2)

    override suspend fun beforeEach(testCase: TestCase) {
        db = Database.connect("jdbc:h2:mem:test${System.currentTimeMillis()};DB_CLOSE_DELAY=-1")
        transaction {
            SchemaUtils.create(*allTables)
        }
    }

    override suspend fun afterEach(testCase: TestCase, result: TestResult) {
        TransactionManager.closeAndUnregister(db)
    }

    init {
        "When load" {
            ExposedCategoriesRepo.load()
        }
        "When save" {
            ExposedCategoriesRepo.insert(listOf(category))
        }
        "When save and load" {
            ExposedCategoriesRepo.insert(listOf(category))

            val categories = ExposedCategoriesRepo.load()

            categories.shouldBeSingleton().first() shouldBe category
        }
        "When delete single" {
            val categoryNonDeleted = category.copy(isDeleted = false)
            ExposedCategoriesRepo.insert(listOf(categoryNonDeleted))

            ExposedCategoriesRepo.delete(listOf(categoryNonDeleted.id))

            ExposedCategoriesRepo.load().shouldBeSingleton().first().isDeleted shouldBe true
        }
        "When delete" {
            val categoryNonDeleted1 = category1.copy(isDeleted = false)
            val categoryNonDeleted2 = category2.copy(isDeleted = false)
            ExposedCategoriesRepo.insert(listOf(categoryNonDeleted1, categoryNonDeleted2))

            ExposedCategoriesRepo.delete(listOf(categoryNonDeleted1.id))

            ExposedCategoriesRepo.load().filter { it.isDeleted }
                .shouldBeSingleton().first().id shouldBe categoryNonDeleted1.id
        }
    }
}
