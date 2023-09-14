package allfit.persistence

import allfit.persistence.domain.CategoryEntity
import allfit.persistence.domain.CheckinEntity
import allfit.persistence.domain.CheckinType
import allfit.persistence.domain.InMemoryCategoriesRepo
import allfit.persistence.domain.InMemoryCheckinsRepository
import allfit.persistence.domain.InMemoryLocationsRepo
import allfit.persistence.domain.InMemoryPartnersRepo
import allfit.persistence.domain.InMemoryReservationsRepo
import allfit.persistence.domain.InMemoryUsageRepository
import allfit.persistence.domain.InMemoryWorkoutsRepo
import allfit.persistence.domain.LocationEntity
import allfit.persistence.domain.PartnerEntity
import allfit.persistence.domain.ReservationEntity
import allfit.persistence.domain.UsageEntity
import allfit.persistence.domain.WorkoutEntity
import allfit.service.SystemClock
import allfit.service.toUtcLocalDateTime
import java.time.LocalDateTime
import java.util.UUID

private val now = SystemClock.now().toUtcLocalDateTime()

private val category1 = CategoryEntity(id = 1, name = "Category 1", slug = "cat1", isDeleted = false)
private val category2 = CategoryEntity(id = 2, name = "Category 2", slug = null, isDeleted = false)
private val allCategories = listOf(category1, category2)

private val partner1 = PartnerEntity(
    id = 1, primaryCategoryId = category1.id, secondaryCategoryIds = emptyList(), name = "Partner 1", slug = "part1",
    description = "description from one fit", note = "note from me", facilities = "fac1,fac2,fac3",
    imageUrl = "www.one.fit/image1.jpg", rating = 0,
    isDeleted = false, isFavorited = true, isHidden = false, isWishlisted = false, locationShortCode = "AMS"
)
private val partner2 = PartnerEntity(
    id = 2, primaryCategoryId = category2.id, secondaryCategoryIds = emptyList(), name = "Partner 2", slug = "part2",
    description = "description from one fit", note = "", facilities = "",
    imageUrl = null, rating = 3,
    isDeleted = false, isFavorited = false, isHidden = false, isWishlisted = true, locationShortCode = "AMS"
)
private val allPartners = listOf(partner1, partner2)

private val workout1 = WorkoutEntity(
    id = 1,
    partnerId = partner1.id,
    name = "Workout 1",
    slug = "workout1",
    about = "about",
    specifics = "specifics",
    teacher = null,
    address = "home street",
    start = now,
    end = now.plusHours(1),
)
val workout2 = WorkoutEntity(
    id = 2,
    partnerId = partner1.id,
    name = "Workout 2",
    slug = "workout2",
    about = "about",
    specifics = "specifics",
    teacher = null,
    address = "home street",
    start = now,
    end = now.plusHours(2),
)
val workout3 = WorkoutEntity(
    id = 3,
    partnerId = partner2.id,
    name = "Workout 3",
    slug = "workout3",
    about = "about",
    specifics = "specifics",
    teacher = null,
    address = "home street",
    start = now,
    end = now.plusHours(3),
)
private val allWorkouts = listOf(workout1, workout2, workout3)

val reservation1 = ReservationEntity(
    uuid = UUID.randomUUID(),
    workoutId = workout1.id,
    workoutStart = LocalDateTime.now()
)
private val allReservations = listOf(reservation1)

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
private val allLocations = listOf(location1)

private val checkin1 = CheckinEntity(
    id = UUID.randomUUID(),
    createdAt = LocalDateTime.now(),
    type = CheckinType.WORKOUT,
    partnerId = partner1.id,
    workoutId = workout1.id,
)

private val allCheckins = listOf(checkin1)

fun InMemoryCategoriesRepo.insertMockData() = apply {
    insertAll(allCategories)
}

fun InMemoryPartnersRepo.insertMockData() = apply {
    insertAll(allPartners)
}

fun InMemoryWorkoutsRepo.insertMockData() = apply {
    insertAll(allWorkouts)
}

fun InMemoryReservationsRepo.insertMockData() = apply {
    insertAll(allReservations)
}

fun InMemoryLocationsRepo.insertMockData() = apply {
    insertAllIfNotYetExists(allLocations)
}

fun InMemoryCheckinsRepository.insertMockData() = apply {
    insertAll(allCheckins)
}

fun InMemoryUsageRepository.insertMockData() = apply {
    upsert(
        UsageEntity(
            total = 3,
            noShows = 1,
            from = LocalDateTime.now().withDayOfMonth(1),
            until = LocalDateTime.now().withDayOfMonth(27),
            periodCap = 16,
            maxCheckInsOrReservationsPerPeriod = 4,
            totalCheckInsOrReservationsPerDay = 2,
            maxReservations = 6,
        )
    )
}
