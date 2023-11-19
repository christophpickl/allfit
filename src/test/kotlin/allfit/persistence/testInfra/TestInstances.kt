package allfit.persistence.testInfra

import allfit.domain.Location
import allfit.persistence.domain.CategoryEntity
import allfit.persistence.domain.CheckinEntity
import allfit.persistence.domain.CheckinType
import allfit.persistence.domain.LocationEntity
import allfit.persistence.domain.PartnerEntity
import allfit.persistence.domain.ReservationEntity
import allfit.persistence.domain.UsageEntity
import allfit.persistence.domain.WorkoutEntity
import io.kotest.property.Arb
import io.kotest.property.arbitrary.Codepoint
import io.kotest.property.arbitrary.alphanumeric
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.double
import io.kotest.property.arbitrary.enum
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.orNull
import io.kotest.property.arbitrary.string
import io.kotest.property.arbitrary.uuid
import java.time.LocalDateTime
import java.util.Random

fun Arb.Companion.categoryEntity() = arbitrary {
    CategoryEntity(
        id = int(min = 1).next(),
        name = string().next(),
        slug = string(minSize = 1, codepoints = Codepoint.alphanumeric()).orNull().next(),
        isDeleted = boolean().next(),
    )
}

fun Arb.Companion.partnerEntity() = arbitrary {
    PartnerEntity(
        id = int(min = 1).next(),
        primaryCategoryId = int(min = 1).next(),
        secondaryCategoryIds = list(int(min = 1), 1..5).next().distinct(),
        name = string().next(),
        slug = string().next(),
        description = string().next(),
        note = string().next(),
        facilities = string().next(),
        imageUrl = string().next(),
        rating = int(min = 0, max = 5).next(),
        isDeleted = boolean().next(),
        isWishlisted = boolean().next(),
        isHidden = boolean().next(),
        isFavorited = boolean().next(),
        locationShortCode = enum<Location>().next().shortCode,
        hasDropins = false,
        hasWorkouts = true,
    )
}

fun Arb.Companion.locationEntity() = arbitrary {
    LocationEntity(
        id = int(min = 1).next(),
        partnerId = int(min = 1).next(),
        streetName = string().next(),
        houseNumber = string().next(),
        addition = string().next(),
        zipCode = string().next(),
        city = string().next(),
        latitude = double(min = 0.0).next(),
        longitude = double(min = 0.0).next(),
    )
}

fun Arb.Companion.workoutEntity() = arbitrary {
    WorkoutEntity(
        id = int(min = 1).next(),
        name = string().next(),
        slug = string(minSize = 1, maxSize = 20, codepoints = Codepoint.alphanumeric()).next(),
        start = LocalDateTime.now(),
        end = LocalDateTime.now().plusHours(1),
        about = string().next(),
        specifics = string().next(),
        teacher = string(maxSize = 10).orNull().next(),
        address = string().next(),
        partnerId = int(min = 1).next(),
    )
}

fun Arb.Companion.reservationEntity() = arbitrary {
    ReservationEntity(
        uuid = uuid().next(),
        workoutId = int(min = 1).next(),
        workoutStart = LocalDateTime.now(),
    )
}

fun Arb.Companion.checkinEntity() = arbitrary {
    if (Random().nextBoolean()) {
        checkinEntityWorkout().next()
    } else {
        checkinEntityDropin().next()
    }
}

fun Arb.Companion.checkinEntityWorkout() = arbitrary {
    CheckinEntity(
        id = uuid().next(),
        createdAt = LocalDateTime.now(),
        type = CheckinType.WORKOUT,
        partnerId = int(min = 1).next(),
        workoutId = int(min = 1).next(),
    )
}

fun Arb.Companion.checkinEntityDropin() = arbitrary {
    CheckinEntity(
        id = uuid().next(),
        createdAt = LocalDateTime.now(),
        type = CheckinType.DROP_IN,
        partnerId = int(min = 1).next(),
        workoutId = null,
    )
}

fun Arb.Companion.usageEntity() = arbitrary {
    UsageEntity(
        total = int(min = 0, max = 10).next(),
        noShows = int(min = 0, max = 10).next(),
        from = LocalDateTime.now(),
        until = LocalDateTime.now(),
        periodCap = int(min = 0, max = 10).next(),
        maxCheckInsOrReservationsPerPeriod = int(min = 0, max = 10).next(),
        totalCheckInsOrReservationsPerDay = int(min = 0, max = 10).next(),
        maxReservations = int(min = 0, max = 10).next(),
    )
}
