package allfit.persistence

import allfit.domain.category
import allfit.domain.partner
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldBeSingleton
import io.kotest.matchers.shouldBe
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

            val category = Arb.category().next()
            ExposedCategoriesRepo.insert(listOf(category))
            ExposedCategoriesRepo.select().shouldBeSingleton().first() shouldBe category

            val partner = Arb.partner().next().copy(categories = listOf(category))
            ExposedPartnersRepo.insert(listOf(partner))
            ExposedPartnersRepo.select().shouldBeSingleton().first() shouldBe partner
        }
    }

    private fun testJdbcUrl(): String {
        val tmpDir = File(System.getProperty("java.io.tmpdir"), "liquitest-${System.currentTimeMillis()}")
        return "jdbc:h2:file:${tmpDir.absolutePath}"
    }
}
