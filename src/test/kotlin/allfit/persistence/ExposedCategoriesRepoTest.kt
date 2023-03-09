package allfit.persistence

import allfit.domain.category
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
    private val category = Arb.category().next()

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
            ExposedCategoriesRepo.save(listOf(category))
        }
        "When save and load" {
            ExposedCategoriesRepo.save(listOf(category))

            val categories = ExposedCategoriesRepo.load()

            categories.categories.shouldBeSingleton().first() shouldBe category
        }
    }
}
