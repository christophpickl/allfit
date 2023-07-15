@file:Suppress("SameParameterValue")

package allfit.presentation.logic

import allfit.TestDates
import allfit.persistence.domain.CategoriesRepo
import allfit.persistence.domain.CategoryEntity
import allfit.persistence.domain.CheckinsRepository
import allfit.persistence.domain.ExposedCategoriesRepo
import allfit.persistence.domain.ExposedCheckinsRepository
import allfit.persistence.domain.ExposedPartnersRepo
import allfit.persistence.domain.ExposedReservationsRepo
import allfit.persistence.domain.ExposedWorkoutsRepo
import allfit.persistence.domain.PartnerEntity
import allfit.persistence.domain.PartnersRepo
import allfit.persistence.domain.ReservationsRepo
import allfit.persistence.domain.WorkoutEntity
import allfit.persistence.domain.WorkoutsRepo
import allfit.persistence.testInfra.DbListener
import allfit.persistence.testInfra.ExposedTestRepo
import allfit.presentation.PartnerModifications
import allfit.presentation.models.Checkin
import allfit.presentation.models.DateRange
import allfit.presentation.models.FullPartner
import allfit.presentation.models.FullWorkout
import allfit.presentation.models.HIDDEN_IMAGE
import allfit.presentation.models.NOT_HIDDEN_IMAGE
import allfit.presentation.models.PartnerCustomAttributesRead
import allfit.presentation.models.SimplePartner
import allfit.presentation.models.SimpleWorkout
import allfit.service.DummyImageStorage
import allfit.service.ImageStorage
import allfit.service.InMemoryImageStorage
import allfit.service.PartnerAndImageBytes
import allfit.service.WorkoutAndImagesBytes
import allfit.service.fromUtcToAmsterdamZonedDateTime
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

    private val categoriesRepo: CategoriesRepo = ExposedCategoriesRepo
    private val reservationsRepo: ReservationsRepo = ExposedReservationsRepo
    private val checkinsRepository: CheckinsRepository = ExposedCheckinsRepository
    private val partnersRepo: PartnersRepo = ExposedPartnersRepo
    private val workoutsRepo: WorkoutsRepo = ExposedWorkoutsRepo

    private fun dataStorageWithImages(withImageStorage: (InMemoryImageStorage) -> Unit): ExposedDataStorage =
        dataStorage(InMemoryImageStorage().also(withImageStorage))

    private fun dataStorage(imageStorage: ImageStorage = DummyImageStorage): ExposedDataStorage = ExposedDataStorage(
        categoriesRepo = categoriesRepo,
        reservationsRepo = reservationsRepo,
        checkinsRepository = checkinsRepository,
        partnersRepo = partnersRepo,
        workoutsRepo = workoutsRepo,
        imageStorage = imageStorage,
        clock = clock
    )

    init {
        extension(DbListener())

        describe("When get categories") {
            it("Given a category Then return it") {
                val category = ExposedTestRepo.insertCategory()

                val categories = dataStorage().getCategories()

                categories shouldContainExactly listOf(category.name)
            }
            it("Given some categories Then return them sorted") {
                val names = listOf("a", "b", "c")
                names.shuffled().forEach { name ->
                    ExposedTestRepo.insertCategory {
                        it.copy(name = name)
                    }
                }

                val categories = dataStorage().getCategories()

                categories shouldContainExactly names
            }
        }

        describe("When get workouts") {
            it("Given workout and requirements Then return it") {
                val (category, partner, workout) = ExposedTestRepo.insertCategoryPartnerAndWorkout()

                val workouts = dataStorageWithImages { imageStorage ->
                    imageStorage.addWorkoutImagesToBeLoaded(workout.toWorkoutAndImagesBytes())
                    imageStorage.addPartnerImagesToBeLoaded(partner.toPartnerAndImageBytes())
                }.getWorkouts()

                val futureFullWorkout = workouts.shouldBeSingleton().first()
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
                ExposedTestRepo.insertCategoryPartnerWorkoutAndReservation()

                val workouts = dataStorage().getWorkouts()

                workouts.shouldBeSingleton().first().simpleWorkout.isReserved shouldBe true
            }

            it("Given workout with checkin Then mark as visited") {
                ExposedTestRepo.insertCategoryPartnerWorkoutAndWorkoutCheckin()

                val workouts = dataStorage().getWorkouts()

                workouts.shouldBeSingleton().first().wasVisited shouldBe true
            }

            it("Given workout with checkin Then partner has checkin count set") {
                ExposedTestRepo.insertCategoryPartnerWorkoutAndWorkoutCheckin()

                val workouts = dataStorage().getWorkouts()

                workouts.shouldBeSingleton().first().partner.checkins shouldBe 1
            }

            it("Given dropin-checkin Then synthetic workout is returned") {
                ExposedTestRepo.insertCategoryPartnerAndDropinCheckin()

                val workouts = dataStorage().getWorkouts()

                workouts.shouldBeSingleton().first().name shouldBe "Drop-In"
            }
        }

        describe("When get workout by ID") {
            it("Given category, partner and workout Then return workout") {
                val (category, partner, workout) = ExposedTestRepo.insertCategoryPartnerAndWorkout()

                val fullWorkout = dataStorage().getWorkoutById(workout.id)

                fullWorkout shouldBe FullWorkout(
                    simpleWorkout = workout.toSimpleWorkout(
                        isReserved = false,
                        image = fullWorkout.image,
                    ),
                    partner = partner.toSimplePartner(
                        image = fullWorkout.partner.image, checkins = 0, categories = listOf(category.name)
                    ),
                )
            }
        }

        describe("When get partner by ID") {
            it("Given no checkins and no workouts Then return it") {
                val (category, partner) = ExposedTestRepo.insertCategoryAndPartner()

                val fullPartner = dataStorage().getPartnerById(partner.id)

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

                val fullPartner = dataStorage().getPartnerById(givenPartner.id)

                fullPartner.upcomingWorkouts.shouldBeSingleton().first().id shouldBe givenWorkout.id
            }

            it("Given past workout for partner with checkin Then return that visited workout") {
                val (_, givenPartner, givenWorkout, _) = ExposedTestRepo.insertCategoryPartnerWorkoutAndWorkoutCheckin { _, p, w ->
                    w.copy(partnerId = p.id, start = past, end = past.plusHours(1))
                }

                val fullPartner = dataStorage().getPartnerById(givenPartner.id)

                fullPartner.checkins shouldBe 1
                fullPartner.pastCheckins.filterIsInstance<Checkin.WorkoutCheckin>().map { it.workout.id }
                    .shouldBeSingleton().first() shouldBe givenWorkout.id
            }

            it("Given past workout for partner with dropin Then return that past checkin") {
                val (_, givenPartner, _) = ExposedTestRepo.insertCategoryPartnerAndDropinCheckin()

                val fullPartner = dataStorage().getPartnerById(givenPartner.id)

                fullPartner.checkins shouldBe 1
                fullPartner.pastCheckins.filterIsInstance<Checkin.DropinCheckin>().map { it.date }.shouldBeSingleton()
            }

            it("Given past workout for partner without checkin Then return empty visited workouts") {
                val (_, givenPartner, _) = ExposedTestRepo.insertCategoryPartnerAndWorkout(withWorkout = { _, p, w ->
                    w.copy(partnerId = p.id, start = past, end = past.plusHours(1))
                })

                val fullPartner = dataStorage().getPartnerById(givenPartner.id)

                fullPartner.checkins shouldBe 0
                fullPartner.pastCheckins.shouldBeEmpty()
            }
        }

        describe("When update partner") {
            it("Given partner Then updated in database") {
                val (_, modifications) = insertPartnerAndGetModifications()

                dataStorage().updatePartner(modifications)

                modifications.assertOn(ExposedPartnersRepo.selectAll().shouldBeSingleton().first())
            }

            it("Given partner Then updated in UI representation") {
                val (partner, modifications) = insertPartnerAndGetModifications()
                val storage = dataStorage()
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
    wasVisited: Boolean = false,
    image: Image,
) = SimpleWorkout(
    id = id,
    partnerId = partnerId,
    name = name,
    about = about,
    specifics = specifics,
    teacher = teacher ?: "",
    address = address,
    date = DateRange(start = start.fromUtcToAmsterdamZonedDateTime(), end = end.fromUtcToAmsterdamZonedDateTime()),
    image = image,
    url = "https://one.fit/en-nl/workouts/$id/$slug",
    isReserved = isReserved,
    wasVisited = wasVisited,
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
    hiddenImage = if (isHidden) HIDDEN_IMAGE else NOT_HIDDEN_IMAGE,
    image = image,
)

private fun PartnerEntity.toFullPartner(
    image: Image,
    checkins: Int,
    categories: List<String>,
    visitedWorkouts: List<SimpleWorkout>,
    upcomingWorkouts: List<SimpleWorkout>,
) = FullPartner(
    pastCheckins = visitedWorkouts.map { Checkin.WorkoutCheckin(it) },
    upcomingWorkouts = upcomingWorkouts,
    simplePartner = toSimplePartner(image, checkins, categories)
)

private fun WorkoutEntity.toWorkoutAndImagesBytes() = WorkoutAndImagesBytes(
    workoutId = id, imageBytes = byteArrayOf()
)

private fun PartnerEntity.toPartnerAndImageBytes() = PartnerAndImageBytes(
    partnerId = id, imageBytes = byteArrayOf()
)

private fun buildFullWorkout(
    workout: WorkoutEntity,
    partner: PartnerEntity,
    category: CategoryEntity,
    workoutImage: Image,
    partnerImage: Image,
    isWorkoutReserved: Boolean,
    partnerCheckins: Int,
    wasVisited: Boolean = false,
) = FullWorkout(
    simpleWorkout = SimpleWorkout(
        id = workout.id,
        partnerId = partner.id,
        name = workout.name,
        about = workout.about,
        specifics = workout.specifics,
        teacher = workout.teacher ?: "",
        address = workout.address,
        date = DateRange(
            start = workout.start.fromUtcToAmsterdamZonedDateTime(), end = workout.end.fromUtcToAmsterdamZonedDateTime()
        ),
        image = workoutImage,
        url = "https://one.fit/en-nl/workouts/${workout.id}/${workout.slug}",
        isReserved = isWorkoutReserved,
        wasVisited = wasVisited,
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
        hiddenImage = if (partner.isHidden) HIDDEN_IMAGE else NOT_HIDDEN_IMAGE,
        image = partnerImage,
    )
)

