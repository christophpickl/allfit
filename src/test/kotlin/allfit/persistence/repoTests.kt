package allfit.persistence

import io.kotest.core.listeners.AfterEachListener
import io.kotest.core.listeners.BeforeEachListener
import io.kotest.core.spec.style.describeSpec
import io.kotest.core.test.TestCase
import io.kotest.core.test.TestResult
import io.kotest.matchers.collections.shouldBeSingleton
import io.kotest.matchers.shouldBe
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction

data class DboContext(
    val id: Int? = null,
    val isDeleted: Boolean? = null,
)

data class RepoTestContext<REPO : Repo<DBO>, DBO>(
    val repo: REPO,
    val dboProvider: (DboContext) -> DBO,
)

class DbListener : BeforeEachListener, AfterEachListener {
    private lateinit var db: Database
    override suspend fun beforeEach(testCase: TestCase) {
        db = Database.connect("jdbc:h2:mem:test${System.currentTimeMillis()};DB_CLOSE_DELAY=-1")
        transaction {
            SchemaUtils.create(*allTables)
        }
    }

    override suspend fun afterEach(testCase: TestCase, result: TestResult) {
        TransactionManager.closeAndUnregister(db)
    }
}

fun <REPO : Repo<DBO>, DBO : Dbo> repoTests(context: RepoTestContext<REPO, DBO>) = describeSpec {
    extension(DbListener())

    with(context) {
        val dbo = dboProvider(DboContext())
        val dbo1 = dboProvider(DboContext(id = 1))
        val dbo2 = dboProvider(DboContext(id = 2))
        val dboNotDeleted = dboProvider(DboContext(isDeleted = false))
        val dboNotDeleted1 = dboProvider(DboContext(id = 1, isDeleted = false))
        val dboNotDeleted2 = dboProvider(DboContext(id = 2, isDeleted = false))

        describe("select") {
            it("success") {
                repo.select()
            }
        }
        describe("insert") {
            it("success") {
                repo.insert(listOf(dbo))

                repo.select().shouldBeSingleton().first() shouldBe dbo
            }
        }
        describe("delete") {
            it("single") {
                repo.insert(listOf(dboNotDeleted))

                repo.delete(listOf(dboNotDeleted.id))

                repo.select().shouldBeSingleton().first().isDeleted shouldBe true
            }
            it("filtered") {
                repo.insert(listOf(dboNotDeleted1, dboNotDeleted2))

                repo.delete(listOf(dboNotDeleted1.id))

                repo.select().filter { it.isDeleted }.shouldBeSingleton().first().isDeleted shouldBe true
            }
        }
    }
}
