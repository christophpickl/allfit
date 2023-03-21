package allfit.persistence

import io.kotest.matchers.collections.shouldBeSingleton
import io.kotest.matchers.shouldBe

fun InMemoryCategoriesRepo.singleShould() =
    categories.values.shouldBeSingleton().first()

infix fun InMemoryPartnersRepo.singletonShouldBe(expected: PartnerEntity) {
    partners.values.shouldBeSingleton().first() shouldBe expected
}

fun InMemoryPartnersRepo.singleShould() =
    partners.values.shouldBeSingleton().first()

infix fun InMemoryWorkoutsRepo.singletonShouldBe(expected: WorkoutEntity) {
    workouts.values.shouldBeSingleton().first() shouldBe expected
}

fun InMemoryWorkoutsRepo.singleShould() =
    workouts.values.shouldBeSingleton().first()

infix fun InMemoryCheckinsRepository.singletonShouldBe(expected: CheckinEntity) {
    checkins.shouldBeSingleton().first() shouldBe expected
}

fun InMemoryCheckinsRepository.singleShould(): CheckinEntity =
    checkins.shouldBeSingleton().first()

