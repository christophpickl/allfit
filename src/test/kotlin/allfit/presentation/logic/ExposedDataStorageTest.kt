@file:Suppress("SameParameterValue")

package allfit.presentation.logic

import allfit.TestDates
import allfit.persistence.domain.*
import allfit.persistence.testInfra.*
import allfit.presentation.PartnerModifications
import allfit.presentation.models.*
import allfit.service.*
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldBeSingleton
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import javafx.scene.image.Image

class ExposedDataStorageTest : StringSpec() {

    private val now = TestDates.now
    private val clock = TestDates.clock

    private fun dataStorageWithImages(withImageStorage: (InMemoryImageStorage) -> Unit): ExposedDataStorage {
        val imageStorage = InMemoryImageStorage().also(withImageStorage)
        return ExposedDataStorage(imageStorage, clock)
    }

    private fun dataStorageWithStaticImages(): ExposedDataStorage =
        ExposedDataStorage(DummyImageStorage, clock)

    init {
        extension(DbListener())

        "Given a cateogry When getAllCategories Then return it" {
            val category = ExposedTestRepo.insertCategory()

            val categories = dataStorageWithStaticImages().getAllCategories()

            categories shouldContainExactly listOf(category.name)
        }

        "Given future workout and requirements When getFutureFullWorkouts Then return it" {
            val (category, partner, workout) = ExposedTestRepo.insertCategoryPartnerAndWorkout {
                it.withFutureStart(now)
            }
            val workoutImage = workout.toWorkoutAndImagesBytes()
            val partnerImage = partner.toPartnerAndImageBytes()

            val futureFullWorkouts = dataStorageWithImages { imageStorage ->
                imageStorage.addWorkoutImagesToBeLoaded(workoutImage)
                imageStorage.addPartnerImagesToBeLoaded(partnerImage)
            }.getFutureFullWorkouts()

            val futureFullWorkout = futureFullWorkouts.shouldBeSingleton().first()
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

        "Given reserved workout When getFutureFullWorkouts Then flag as reserved" {
            ExposedTestRepo.insertCategoryPartnerWorkoutAndReservation(
                withWorkout = {
                    it.withFutureStart(now)
                },
                withReservation = {
                    it.withFutureStart(now)
                }
            )

            val workouts = dataStorageWithStaticImages().getFutureFullWorkouts()

            workouts.shouldBeSingleton().first().simpleWorkout.isReserved shouldBe true
        }

        "Given workout with partner with checkin When getFutureFullWorkouts Then partner has checkin count set" {
            ExposedTestRepo.insertCategoryPartnerWorkoutAndWorkoutCheckin(withWorkout = {
                it.withFutureStart(now)
            })

            val workouts = dataStorageWithStaticImages().getFutureFullWorkouts()

            workouts.shouldBeSingleton().first().partner.checkins shouldBe 1
        }

        "Given no checkins and no workouts When getFullPartnerById Then return it" {
            val (category, partner) = ExposedTestRepo.insertCategoryAndPartner()

            val fullPartner = dataStorageWithStaticImages().getFullPartnerById(partner.id)

            fullPartner shouldBe partner.toFullPartner(
                image = fullPartner.image,
                checkins = 0,
                categories = listOf(category.name),
                pastWorkouts = emptyList(),
                currentWorkouts = emptyList(),
            )
        }

        "Given checkins and past and current workouts When getFullPartnerById Then return it" {
            val (_, partner) = ExposedTestRepo.insertCategoryAndPartner()
            val past = now.minusDays(1)
            val pastWorkout = Arb.workoutEntity().next()
                .copy(partnerId = partner.id, start = past, end = past.plusHours(1))
            ExposedWorkoutsRepo.insertAll(listOf(pastWorkout))
            val future = now.plusDays(1)
            val currentWorkout = Arb.workoutEntity().next()
                .copy(partnerId = partner.id, start = future, end = future.plusHours(1))
            ExposedWorkoutsRepo.insertAll(listOf(currentWorkout))
            ExposedCheckinsRepository.insertAll(
                listOf(
                    Arb.checkinEntityWorkout().next().copy(partnerId = partner.id, workoutId = pastWorkout.id)
                )
            )

            val fullPartner = dataStorageWithStaticImages().getFullPartnerById(partner.id)

            fullPartner.checkins shouldBe 1
            fullPartner.pastWorkouts.shouldBeSingleton().first().id shouldBe pastWorkout.id
            fullPartner.currentWorkouts.shouldBeSingleton().first().id shouldBe currentWorkout.id
        }

        "Given workout When toFullWorkout Then return it" {
            val (category, partner, workout) = ExposedTestRepo.insertCategoryPartnerAndWorkout()

            val fullWorkout = dataStorageWithStaticImages().getFullWorkoutById(workout.id)

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

        "Given partner When updatePartner Then updated in database" {
            val (_, modifications) = insertPartnerAndGetModifications()

            dataStorageWithStaticImages().updatePartner(modifications)

            modifications.assertOn(ExposedPartnersRepo.selectAll().shouldBeSingleton().first())
        }

        "Given partner When updatePartner Then updated in UI representation" {
            val (partner, modifications) = insertPartnerAndGetModifications()
            val storage = dataStorageWithStaticImages()
            storage.getFullPartnerById(partner.id) // prefetch so it will be stored

            storage.updatePartner(modifications)

            modifications.assertOn(storage.getFullPartnerById(partner.id).simplePartner.also {
                println(it)
            })
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
    pastWorkouts: List<SimpleWorkout>,
    currentWorkouts: List<SimpleWorkout>,
) = FullPartner(
    pastWorkouts = pastWorkouts,
    currentWorkouts = currentWorkouts,
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
