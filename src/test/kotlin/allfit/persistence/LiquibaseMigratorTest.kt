package allfit.persistence

import allfit.domain.category
import io.kotest.core.spec.style.StringSpec
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import org.jetbrains.exposed.sql.Database
import java.io.File

class LiquibaseMigratorTest : StringSpec() {
    init {
        "When migrate Then load and save works" {
            val jdbcUrl = testJdbcUrl()
            LiquibaseMigrator.migrate(LiquibaseConfig("", "", jdbcUrl))

            Database.connect(jdbcUrl)
            ExposedCategoriesRepo.save(listOf(Arb.category().next()))
            ExposedCategoriesRepo.load()
        }
    }

    private fun testJdbcUrl(): String {
        val tmpDir = File(System.getProperty("java.io.tmpdir"), "liquitest-${System.currentTimeMillis()}")
        return "jdbc:h2:file:${tmpDir.absolutePath}"
    }
}
