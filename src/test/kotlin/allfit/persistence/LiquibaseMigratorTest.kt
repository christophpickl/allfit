package allfit.persistence

import allfit.persistence.domain.ExposedCategoriesRepo
import allfit.persistence.domain.ExposedCheckinsRepository
import allfit.persistence.domain.ExposedLocationsRepo
import allfit.persistence.domain.ExposedPartnersRepo
import allfit.persistence.domain.ExposedReservationsRepo
import allfit.persistence.domain.ExposedUsageRepository
import allfit.persistence.domain.ExposedWorkoutsRepo
import allfit.persistence.testInfra.categoryEntity
import allfit.persistence.testInfra.checkinEntityWorkout
import allfit.persistence.testInfra.locationEntity
import allfit.persistence.testInfra.partnerEntity
import allfit.persistence.testInfra.reservationEntity
import allfit.persistence.testInfra.usageEntity
import allfit.persistence.testInfra.workoutEntity
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldBeSingleton
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import org.jetbrains.exposed.sql.Database

class LiquibaseMigratorTest : StringSpec() {
    init {
        "When migrate Then load and save works" {
            val jdbcUrl = testJdbcUrl()
            LiquibaseMigrator.migrate(LiquibaseConfig("", "", jdbcUrl))
            Database.connect(jdbcUrl)

            val category = Arb.categoryEntity().next()
            ExposedCategoriesRepo.insertAll(listOf(category))
            ExposedCategoriesRepo.selectAll().shouldBeSingleton().first() shouldBe category

            val partner = Arb.partnerEntity().next()
                .copy(primaryCategoryId = category.id, secondaryCategoryIds = emptyList())
            ExposedPartnersRepo.insertAll(listOf(partner))
            ExposedPartnersRepo.selectAll().shouldBeSingleton().first() shouldBe partner

            val location = Arb.locationEntity().next().copy(partnerId = partner.id)
            ExposedLocationsRepo.insertAllIfNotYetExists(listOf(location))
            ExposedLocationsRepo.selectAll().shouldBeSingleton().first() shouldBe location

            val workout = Arb.workoutEntity().next().copy(partnerId = partner.id)
            ExposedWorkoutsRepo.insertAll(listOf(workout))
            ExposedWorkoutsRepo.selectAllStartingFrom(fromInclusive = workout.start).shouldBeSingleton()
                .first() shouldBe workout

            val reservation = Arb.reservationEntity().next().copy(workoutId = workout.id)
            ExposedReservationsRepo.insertAll(listOf(reservation))
            ExposedReservationsRepo.selectAll().shouldBeSingleton()
                .first() shouldBe reservation

            val checkin = Arb.checkinEntityWorkout().next().copy(workoutId = workout.id, partnerId = partner.id)
            ExposedCheckinsRepository.insertAll(listOf(checkin))
            ExposedCheckinsRepository.selectAll().shouldBeSingleton().first() shouldBe checkin

            val usage = Arb.usageEntity().next()
            ExposedUsageRepository.upsert(usage)
            ExposedUsageRepository.selectOne() shouldBe usage
        }
    }

    private fun testJdbcUrl(): String =
        "jdbc:h2:mem:liquitest-${System.currentTimeMillis()};DB_CLOSE_DELAY=-1"
}
