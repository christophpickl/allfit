package allfit.persistence.testInfra

import allfit.persistence.domain.CategoriesTable
import allfit.persistence.domain.CategoryEntity
import allfit.persistence.domain.CheckinEntity
import allfit.persistence.domain.ExposedCategoriesRepo
import allfit.persistence.domain.ExposedCheckinsRepository
import allfit.persistence.domain.ExposedPartnersRepo
import allfit.persistence.domain.ExposedReservationsRepo
import allfit.persistence.domain.ExposedWorkoutsRepo
import allfit.persistence.domain.PartnerEntity
import allfit.persistence.domain.ReservationEntity
import allfit.persistence.domain.WorkoutEntity
import allfit.service.Quadrupel
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

object ExposedTestRepo {

    fun insertCategory(
        withCategory: (CategoryEntity) -> CategoryEntity = { it },
    ): CategoryEntity {
        val categoryToBeInserted = Arb.categoryEntity().next().let(withCategory)
        ExposedCategoriesRepo.insertAll(listOf(categoryToBeInserted))
        return categoryToBeInserted
    }

    private fun insertCategoryIfNotExists(categories: List<CategoryEntity>) {
        transaction {
            val categoryIds = categories.map { it.id }
            val existingCategoryIds = CategoriesTable.select { CategoriesTable.id inList categoryIds }
                .map { it[CategoriesTable.id].value }
            val toInsertCategoryIds = categoryIds.subtract(existingCategoryIds.toSet())
            val toInsertCategories = categories.filter { toInsertCategoryIds.contains(it.id) }
            ExposedCategoriesRepo.insertAll(toInsertCategories)
        }
    }

    fun insertCategoryAndPartner(
        withCategory: (CategoryEntity) -> CategoryEntity = { it },
        withPartner: (PartnerEntity) -> PartnerEntity = { it },
    ): Pair<CategoryEntity, PartnerEntity> {
        val category = insertCategory(withCategory)
        val partner = Arb.partnerEntity().next().let(withPartner).copy(
            primaryCategoryId = category.id,
            secondaryCategoryIds = emptyList(),
        )
        ExposedPartnersRepo.insertAll(listOf(partner))
        return Pair(category, partner)
    }

    fun insertCategoryIfNotExistingAndPartner(
        withPartner: (PartnerEntity) -> PartnerEntity = { it }
    ): PartnerEntity {
        val category = Arb.categoryEntity().next()
        val partner = Arb.partnerEntity().next()

        insertCategoryIfNotExists(listOf(category))
        val partnerWithCategory = partner.copy(
            primaryCategoryId = category.id,
            secondaryCategoryIds = emptyList(),
        ).let(withPartner)

        ExposedPartnersRepo.insertAll(listOf(partnerWithCategory))
        return partnerWithCategory
    }

    fun insertCategoryPartnerAndWorkout(
        withCategory: (CategoryEntity) -> CategoryEntity = { it },
        withPartner: (PartnerEntity) -> PartnerEntity = { it },
        withWorkout: (WorkoutEntity) -> WorkoutEntity = { it },
    ): Triple<CategoryEntity, PartnerEntity, WorkoutEntity> {
        val (category, partner) = insertCategoryAndPartner(withCategory, withPartner)

        val workout = Arb.workoutEntity().next().let(withWorkout).copy(
            partnerId = partner.id,
//            start = LocalDateTime.now().plusDays(1)
        )
        ExposedWorkoutsRepo.insertAll(listOf(workout))

        return Triple(category, partner, workout)
    }

    fun insertCategoryAndPartnerForWorkout(
        withCategory: (CategoryEntity) -> CategoryEntity = { it },
        withPartner: (PartnerEntity) -> PartnerEntity = { it },
        withWorkout: (WorkoutEntity) -> WorkoutEntity = { it },
    ): WorkoutEntity {
        val (_, partner) = insertCategoryAndPartner(withCategory, withPartner)

        return Arb.workoutEntity().next().let(withWorkout).copy(partnerId = partner.id)
    }

    fun insertCategoryPartnerWorkoutAndCheckin(
        withCategory: (CategoryEntity) -> CategoryEntity = { it },
        withPartner: (PartnerEntity) -> PartnerEntity = { it },
        withWorkout: (WorkoutEntity) -> WorkoutEntity = { it },
    ): Quadrupel<CategoryEntity, PartnerEntity, WorkoutEntity, CheckinEntity> {
        val (category, partner, workout) = insertCategoryPartnerAndWorkout(withCategory, withPartner, withWorkout)
        val checkin = Arb.checkinEntity().next().copy(workoutId = workout.id)
        ExposedCheckinsRepository.insertAll(listOf(checkin))
        return Quadrupel(category, partner, workout, checkin)
    }

    fun insertCategoryPartnerAndWorkoutForReservation(
        withCategory: (CategoryEntity) -> CategoryEntity = { it },
        withPartner: (PartnerEntity) -> PartnerEntity = { it },
        withWorkout: (WorkoutEntity) -> WorkoutEntity = { it },
        withReservation: (ReservationEntity) -> ReservationEntity = { it },
    ): ReservationEntity {
        val (_, _, workout) = insertCategoryPartnerAndWorkout(withCategory, withPartner, withWorkout)

        return Arb.reservationEntity().next().let(withReservation).copy(workoutId = workout.id)
    }

    fun insertCategoryPartnerWorkoutAndReservation(
        withCategory: (CategoryEntity) -> CategoryEntity = { it },
        withPartner: (PartnerEntity) -> PartnerEntity = { it },
        withWorkout: (WorkoutEntity) -> WorkoutEntity = { it },
        withReservation: (ReservationEntity) -> ReservationEntity = { it },
    ): ReservationEntity {
        val reservation = insertCategoryPartnerAndWorkoutForReservation(
            withCategory,
            withPartner,
            withWorkout,
            withReservation
        )
        ExposedReservationsRepo.insertAll(listOf(reservation))
        return reservation
    }

}
