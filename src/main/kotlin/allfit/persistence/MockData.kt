package allfit.persistence

import allfit.service.SystemClock
import allfit.service.toUtcLocalDateTime

private val category1 = CategoryEntity(id = 1, name = "Category 1", isDeleted = false)
private val category2 = CategoryEntity(id = 2, name = "Category 2", isDeleted = false)
private val categories = listOf(category1, category2)
private val partner1 = PartnerEntity(id = 1, name = "Partner 1", isDeleted = false, categoryIds = listOf(category1.id))
private val partner2 = PartnerEntity(id = 2, name = "Partner 2", isDeleted = false, categoryIds = listOf(category2.id))
private val partners = listOf(partner1, partner2)
private val now = SystemClock.now().toUtcLocalDateTime()
private val workouts = listOf(
    WorkoutEntity(
        id = 1,
        name = "Workout 1",
        slug = "workout1",
        start = now,
        end = now.plusHours(1),
        partnerId = partner1.id
    ),
    WorkoutEntity(
        id = 2,
        name = "Workout 2",
        slug = "workout2",
        start = now,
        end = now.plusHours(2),
        partnerId = partner1.id
    ),
    WorkoutEntity(
        id = 3,
        name = "Workout 3",
        slug = "workout3",
        start = now,
        end = now.plusHours(3),
        partnerId = partner2.id
    ),
)

fun InMemoryCategoriesRepo.insertMockData() = apply {
    insertAll(categories)
}

fun InMemoryPartnersRepo.insertMockData() = apply {
    insertAll(partners)
}

fun InMemoryWorkoutsRepo.insertMockData() = apply {
    insertAll(workouts)
}
