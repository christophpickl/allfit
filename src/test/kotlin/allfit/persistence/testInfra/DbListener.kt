package allfit.persistence.testInfra

import allfit.persistence.allTables
import io.kotest.core.listeners.AfterEachListener
import io.kotest.core.listeners.BeforeEachListener
import io.kotest.core.test.TestCase
import io.kotest.core.test.TestResult
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction

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
