@file:Suppress("SameParameterValue")

package allfit.presentation.logic

import allfit.TestDates
import allfit.persistence.domain.CategoryEntity
import allfit.persistence.domain.ExposedPartnersRepo
import allfit.persistence.domain.PartnerEntity
import allfit.persistence.domain.WorkoutEntity
import allfit.persistence.testInfra.DbListener
import allfit.persistence.testInfra.ExposedTestRepo
import allfit.persistence.testInfra.withFutureStart
import allfit.presentation.PartnerModifications
import allfit.presentation.models.*
import allfit.service.*
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldBeSingleton
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import javafx.scene.image.Image

class ExposedDataStorageTest : DescribeSpec() {

    private val now = TestDates.now
    private val past = now.minusDays(1)
    private val future = now.plusDays(1)
    private val clock = TestDates.clock

    private fun dataStorageWithImages(withImageStorage: (InMemoryImageStorage) -> Unit): ExposedDataStorage {
        val imageStorage = InMemoryImageStorage().also(withImageStorage)
        return ExposedDataStorage(imageStorage, clock)
    }

    private fun dataStorageWithStaticImages(): ExposedDataStorage =
        ExposedDataStorage(DummyImageStorage, clock)

    init {
        extension(DbListener())

        describe("When getCategories") {
            it("Given a category Then return it") {
                val category = ExposedTestRepo.insertCategory()

                val categories = dataStorageWithStaticImages().getCategories()

                categories shouldContainExactly listOf(category.name)
            }
            it("Given some categories Then return them sorted") {
                val names = listOf("a", "b", "c")
                names.shuffled().forEach { name ->
                    ExposedTestRepo.insertCategory {
                        it.copy(name = name)
                    }
                }

                val categories = dataStorageWithStaticImages().getCategories()

                categories shouldContainExactly names
            }
        }

        describe("When getUpcomingWorkouts") {
            it("Given future workout and requirements  Then return it") {
                val (category, partner, workout) = ExposedTestRepo.insertCategoryPartnerAndWorkout { _, _, w ->
                    w.withFutureStart(now)
                }
                val workoutImage = workout.toWorkoutAndImagesBytes()
                val partnerImage = partner.toPartnerAndImageBytes()

                val upcomingFullWorkouts = dataStorageWithImages { imageStorage ->
                    imageStorage.addWorkoutImagesToBeLoaded(workoutImage)
                    imageStorage.addPartnerImagesToBeLoaded(partnerImage)
                }.getUpcomingWorkouts()

                val futureFullWorkout = upcomingFullWorkouts.shouldBeSingleton().first()
                futureFullWorkout shouldBe buildFullWorkout(
                    workout = workout,
                    partner = partner,
                    category = category,
                    workoutImage = futureFullWorkout.image,
                    partnerImage = futureFullWorkout.partner.image,
                    isWorkoutReserved = false,
                    partnerCheckins = 0,
                )
            }

            it("Given reserved workout Then flag as reserved") {
                ExposedTestRepo.insertCategoryPartnerWorkoutAndReservation(
                    withWorkout = { _, _, w ->
                        w.withFutureStart(now)
                    },
                    withReservation = {
                        it.withFutureStart(now)
                    }
                )

                val workouts = dataStorageWithStaticImages().getUpcomingWorkouts()

                workouts.shouldBeSingleton().first().simpleWorkout.isReserved shouldBe true
            }

            it("Given workout with partner with checkin Then partner has checkin count set") {
                ExposedTestRepo.insertCategoryPartnerWorkoutAndWorkoutCheckin(withWorkout = { _, _, w ->
                    w.withFutureStart(now)
                })

                val workouts = dataStorageWithStaticImages().getUpcomingWorkouts()

                workouts.shouldBeSingleton().first().partner.checkins shouldBe 1
            }
        }

        describe("When getPartnerById") {
            it("Given no checkins and no workouts Then return it") {
                val (category, partner) = ExposedTestRepo.insertCategoryAndPartner()

                val fullPartner = dataStorageWithStaticImages().getPartnerById(partner.id)

                fullPartner shouldBe partner.toFullPartner(
                    image = fullPartner.image,
                    checkins = 0,
                    categories = listOf(category.name),
                    visitedWorkouts = emptyList(),
                    upcomingWorkouts = emptyList(),
                )
            }

            it("Given future workout for partner Then return that upcoming workout") {
                val (_, givenPartner, givenWorkout) = ExposedTestRepo.insertCategoryPartnerAndWorkout(withWorkout = { _, p, w ->
                    w.copy(partnerId = p.id, start = future, end = future.plusHours(1))
                })

                val fullPartner = dataStorageWithStaticImages().getPartnerById(givenPartner.id)

                fullPartner.upcomingWorkouts.shouldBeSingleton().first().id shouldBe givenWorkout.id
            }

            it("Given past workout for partner with checkin Then return that visited workout") {
                val (_, givenPartner, givenWorkout, _) = ExposedTestRepo.insertCategoryPartnerWorkoutAndWorkoutCheckin { _, p, w ->
                    w.copy(partnerId = p.id, start = past, end = past.plusHours(1))
                }

                val fullPartner = dataStorageWithStaticImages().getPartnerById(givenPartner.id)

                fullPartner.checkins shouldBe 1
                fullPartner.visitedWorkouts.map { it.id }.shouldBeSingleton().first() shouldBe givenWorkout.id
            }

            it("Given past workout for partner without checkin Then return empty visited workouts") {
                val (_, givenPartner, _) = ExposedTestRepo.insertCategoryPartnerAndWorkout(withWorkout = { _, p, w ->
                    w.copy(partnerId = p.id, start = past, end = past.plusHours(1))
                })

                val fullPartner = dataStorageWithStaticImages().getPartnerById(givenPartner.id)

                fullPartner.checkins shouldBe 0
                fullPartner.visitedWorkouts.shouldBeEmpty()
            }
        }

        describe("When getWorkoutById") {
            it("Given category, partner and workout Then return workout") {
                val (category, partner, workout) = ExposedTestRepo.insertCategoryPartnerAndWorkout()

                val fullWorkout = dataStorageWithStaticImages().getWorkoutById(workout.id)

                fullWorkout shouldBe FullWorkout(
                    simpleWorkout = workout.toSimpleWorkout(
                        isReserved = false,
                        image = fullWorkout.image,
                    ),
                    partner = partner.toSimplePartner(
                        image = fullWorkout.partner.image,
                        checkins = 0,
                        categories = listOf(category.name)
                    ),
                )
            }
        }

        describe("When updatePartner") {
            it("Given partner  Then updated in database") {
                val (_, modifications) = insertPartnerAndGetModifications()

                dataStorageWithStaticImages().updatePartner(modifications)

                modifications.assertOn(ExposedPartnersRepo.selectAll().shouldBeSingleton().first())
            }

            it("Given partner Then updated in UI representation") {
                val (partner, modifications) = insertPartnerAndGetModifications()
                val storage = dataStorageWithStaticImages()
                storage.getPartnerById(partner.id) // prefetch so it will be stored

                storage.updatePartner(modifications)

                modifications.assertOn(storage.getPartnerById(partner.id).simplePartner.also {
                    println(it)
                })
            }
        }
    }
}

private fun PartnerModifications.assertOn(partner: PartnerCustomAttributesRead) {
    partner.rating shouldBe rating
    partner.note shouldBe note
    partner.isFavorited shouldBe isFavorited
    partner.isWishlisted shouldBe isWishlisted
}

private fun insertPartnerAndGetModifications(): Pair<PartnerEntity, PartnerModifications> {
    val (_, partner) = ExposedTestRepo.insertCategoryAndPartner(withPartner = {
        it.copy(
            rating = 0,
            note = "old",
            isFavorited = false,
            isWishlisted = false,
        )
    })
    val modifications = PartnerModifications(
        partnerId = partner.id,
        rating = 1,
        note = "new",
        isFavorited = true,
        isWishlisted = true,
    )
    return Pair(partner, modifications)
}

private fun WorkoutEntity.toSimpleWorkout(
    isReserved: Boolean,
    image: Image
) = SimpleWorkout(
    id = id,
    partnerId = partnerId,
    name = name,
    about = about,
    specifics = specifics,
    address = address,
    date = DateRange(start = start.fromUtcToAmsterdamZonedDateTime(), end = end.fromUtcToAmsterdamZonedDateTime()),
    image = image,
    url = "https://one.fit/en-nl/workouts/$id/$slug",
    isReserved = isReserved,
)

private fun PartnerEntity.toSimplePartner(
    image: Image,
    checkins: Int,
    categories: List<String>,
) = SimplePartner(
    categories = categories,
    checkins = checkins,
    url = "https://one.fit/en-nl/partners/${id}/${slug}",
    id = id,
    name = name,
    note = note,
    description = description,
    facilities = facilities,
    rating = rating,
    isFavorited = isFavorited,
    isWishlisted = isWishlisted,
    isHidden = isHidden,
    image = image,
)

private fun PartnerEntity.toFullPartner(
    image: Image,
    checkins: Int,
    categories: List<String>,
    visitedWorkouts: List<SimpleWorkout>,
    upcomingWorkouts: List<SimpleWorkout>,
) = FullPartner(
    visitedWorkouts = visitedWorkouts,
    upcomingWorkouts = upcomingWorkouts,
    simplePartner = toSimplePartner(image, checkins, categories)
)

private fun WorkoutEntity.toWorkoutAndImagesBytes() = WorkoutAndImagesBytes(
    workoutId = id,
    imageBytes = byteArrayOf()
)

private fun PartnerEntity.toPartnerAndImageBytes() = PartnerAndImageBytes(
    partnerId = id,
    imageBytes = byteArrayOf()
)

private fun buildFullWorkout(
    workout: WorkoutEntity,
    partner: PartnerEntity,
    category: CategoryEntity,
    workoutImage: Image,
    partnerImage: Image,
    isWorkoutReserved: Boolean,
    partnerCheckins: Int,
) = FullWorkout(
    simpleWorkout = SimpleWorkout(
        id = workout.id,
        partnerId = partner.id,
        name = workout.name,
        about = workout.about,
        specifics = workout.specifics,
        address = workout.address,
        date = DateRange(
            start = workout.start.fromUtcToAmsterdamZonedDateTime(),
            end = workout.end.fromUtcToAmsterdamZonedDateTime()
        ),
        image = workoutImage,
        url = "https://one.fit/en-nl/workouts/${workout.id}/${workout.slug}",
        isReserved = isWorkoutReserved,
    ), partner = SimplePartner(
        id = partner.id,
        name = partner.name,
        url = "https://one.fit/en-nl/partners/${partner.id}/${partner.slug}",
        categories = listOf(category.name),
        note = partner.note,
        description = partner.description,
        facilities = partner.facilities,
        checkins = partnerCheckins,
        rating = partner.rating,
        isFavorited = partner.isFavorited,
        isWishlisted = partner.isWishlisted,
        isHidden = partner.isHidden,
        image = partnerImage,
    )
)

