package allfit.persistence

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

            ExposedCategoriesRepo.insert(listOf(Arb.categoryDbo().next()))
            ExposedCategoriesRepo.select()
            ExposedPartnersRepo.insert(listOf(Arb.partnerDbo().next()))
            ExposedPartnersRepo.select()
        }
    }

    private fun testJdbcUrl(): String {
        val tmpDir = File(System.getProperty("java.io.tmpdir"), "liquitest-${System.currentTimeMillis()}")
        return "jdbc:h2:file:${tmpDir.absolutePath}"
    }
}
