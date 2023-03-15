package allfit.persistence

import allfit.service.SystemClock
import allfit.service.toUtcLocalDateTime
import java.time.LocalDateTime
import java.util.UUID

private val now = SystemClock.now().toUtcLocalDateTime()

private val category1 = CategoryEntity(id = 1, name = "Category 1", slug = "cat1", isDeleted = false)
private val category2 = CategoryEntity(id = 2, name = "Category 2", slug = null, isDeleted = false)
private val categories = listOf(category1, category2)

private val partner1 = PartnerEntity(
    id = 1, categoryIds = listOf(category1.id), name = "Partner 1", slug = "part1",
    description = "description from one fit", note = "note from me", facilities = "fac1,fac2,fac3",
    imageUrl = "www.one.fit/image1.jpg",
    isDeleted = false, isFavorited = true, isHidden = false, isStarred = false
)
private val partner2 = PartnerEntity(
    id = 2, categoryIds = listOf(category2.id), name = "Partner 2", slug = "part2",
    description = "description from one fit", note = "", facilities = "", imageUrl = "www.one.fit/image2.jpg",
    isDeleted = false, isFavorited = false, isHidden = false, isStarred = true
)
private val partners = listOf(partner1, partner2)

private val workout1 = WorkoutEntity(
    id = 1, partnerId = partner1.id,
    name = "Workout 1", slug = "workout1", about = "about", specifics = "specifics", address = "home street",
    start = now, end = now.plusHours(1),
)
val workout2 = WorkoutEntity(
    id = 2, partnerId = partner1.id,
    name = "Workout 2", slug = "workout2", about = "about", specifics = "specifics", address = "home street",
    start = now, end = now.plusHours(2),
)
val workout3 = WorkoutEntity(
    id = 3, partnerId = partner2.id,
    name = "Workout 3", slug = "workout3", about = "about", specifics = "specifics", address = "home street",
    start = now, end = now.plusHours(3),
)
private val workouts = listOf(workout1, workout2, workout3)

val reservation1 = ReservationEntity(
    uuid = UUID.randomUUID(),
    workoutId = workout1.id,
    workoutStart = LocalDateTime.now()
)
private val reservations = listOf(reservation1)

private val location1 = LocationEntity(
    id = 1,
    partnerId = partner1.id,
    streetName = "Main Street",
    houseNumber = "42",
    addition = "8",
    zipCode = "1000 AB",
    city = "Amsterdam",
    latitude = 100.0,
    longitude = 200.0,
)
private val locations = listOf(location1)

fun InMemoryCategoriesRepo.insertMockData() = apply {
    insertAll(categories)
}

fun InMemoryPartnersRepo.insertMockData() = apply {
    insertAll(partners)
}

fun InMemoryWorkoutsRepo.insertMockData() = apply {
    insertAll(workouts)
}

fun InMemoryReservationsRepo.insertMockData() = apply {
    insertAll(reservations)
}

fun InMemoryLocationsRepo.insertMockData() = apply {
    insertAll(locations)
}
