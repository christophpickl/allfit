package allfit.persistence

import allfit.domain.Category
import allfit.domain.Partner
import allfit.domain.Workout

private val category1 = Category(id = 1, name = "Category 1", isDeleted = false)
private val category2 = Category(id = 2, name = "Category 2", isDeleted = false)
private val categories = listOf(category1, category2)
private val partner1 = Partner(id = 1, name = "Partner 1", isDeleted = false, categories = listOf(category1))
private val partner2 = Partner(id = 2, name = "Partner 2", isDeleted = false, categories = listOf(category2))
private val partners = listOf(partner1, partner2)
private val workouts = listOf(
    Workout(id = 1, name = "Workout 1", slug = "workout1", partner = partner1),
    Workout(id = 2, name = "Workout 2", slug = "workout2", partner = partner1),
    Workout(id = 3, name = "Workout 3", slug = "workout3", partner = partner2),
)

fun InMemoryCategoriesRepo.insertMockData() = apply {
    insert(categories)
}

fun InMemoryPartnersRepo.insertMockData() = apply {
    insert(partners)
}

fun InMemoryWorkoutsRepo.insertMockData() = apply {
    insert(workouts)
}
