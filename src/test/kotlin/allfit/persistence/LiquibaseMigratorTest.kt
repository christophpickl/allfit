package allfit.persistence

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

            val category = Arb.categoryEntity().next()
            ExposedCategoriesRepo.insertAll(listOf(category))
            ExposedCategoriesRepo.selectAll().shouldBeSingleton().first() shouldBe category

            val partner = Arb.partnerEntity().next().copy(categoryIds = listOf(category.id))
            ExposedPartnersRepo.insertAll(listOf(partner))
            ExposedPartnersRepo.selectAll().shouldBeSingleton().first() shouldBe partner

            val workout = Arb.workoutEntity().next().copy(partnerId = partner.id)
            ExposedWorkoutsRepo.insertAll(listOf(workout))
            ExposedWorkoutsRepo.selectAllStartingFrom(fromInclusive = workout.start).shouldBeSingleton()
                .first() shouldBe workout
        }
    }

    private fun testJdbcUrl(): String {
        val tmpDir = File(System.getProperty("java.io.tmpdir"), "liquitest-${System.currentTimeMillis()}")
        return "jdbc:h2:file:${tmpDir.absolutePath}"
    }
}
