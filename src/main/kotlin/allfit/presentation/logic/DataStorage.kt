package allfit.presentation.logic

import allfit.api.OnefitUtils
import allfit.persistence.domain.ExposedCategoriesRepo
import allfit.persistence.domain.ExposedCheckinsRepository
import allfit.persistence.domain.ExposedPartnersRepo
import allfit.persistence.domain.ExposedReservationsRepo
import allfit.persistence.domain.ExposedWorkoutsRepo
import allfit.persistence.domain.PartnerEntity
import allfit.persistence.domain.WorkoutEntity
import allfit.presentation.PartnerModifications
import allfit.presentation.models.DateRange
import allfit.presentation.models.FullPartner
import allfit.presentation.models.FullWorkout
import allfit.presentation.models.SimplePartner
import allfit.presentation.models.SimpleWorkout
import allfit.service.ImageStorage
import allfit.service.SystemClock
import allfit.service.fromUtcToAmsterdamZonedDateTime
import javafx.scene.image.Image

interface DataStorage {
    fun getFutureFullWorkouts(): List<FullWorkout>
    fun getFullPartnerById(partnerId: Int): FullPartner

    // for real impl, here we do database operations
    fun updatePartner(modifications: PartnerModifications)
    fun getFullWorkoutById(workoutId: Int): FullWorkout
    fun getAllCategories(): List<String>
}

class ExposedDataStorage(
    private val imageStorage: ImageStorage,
) : DataStorage {

    private val simplePartners by lazy {
        val categoriesById = ExposedCategoriesRepo.selectAll().associateBy { it.id }
        val partnerEntities = ExposedPartnersRepo.selectAll()
        val partnerImagesByPartnerId = imageStorage.loadPartnerImages(partnerEntities.map { it.id })
            .associateBy { it.partnerId }

        val checkinsByPartnerId = ExposedCheckinsRepository.selectCountForPartners().associateBy { it.partnerId }

        partnerEntities.map { partnerEntity ->
            partnerEntity.toSimplePartner(
                image = Image(partnerImagesByPartnerId[partnerEntity.id]!!.inputStream()),
                categories = mutableListOf<String>().also { list ->
                    list.add(categoriesById[partnerEntity.primaryCategoryId]!!.name)
                    list.addAll(partnerEntity.secondaryCategoryIds.map { secondaryCategoryId ->
                        categoriesById[secondaryCategoryId]!!.name
                    })
                },
                url = OnefitUtils.partnerUrl(partnerEntity.id, partnerEntity.slug),
                checkins = checkinsByPartnerId[partnerEntity.id]!!.checkinsCount,
            )
        }
    }

    private val simplePartnersById by lazy {
        simplePartners.associateBy { it.id }
    }

    private val simpleWorkouts by lazy {
        val reservations = ExposedReservationsRepo.selectAll().map { it.workoutId }.toSet()
        val workoutEntities = ExposedWorkoutsRepo.selectAll()
        val workoutImages = imageStorage.loadWorkoutImages(workoutEntities.map { it.id }).associateBy { it.workoutId }
        workoutEntities.map { workoutEntity ->
            workoutEntity.toSimpleWorkout(
                isReserved = reservations.contains(workoutEntity.id),
                image = Image(workoutImages[workoutEntity.id]!!.inputStream())
            )
        }
    }

    private val futureSimpleWorkouts by lazy {
        val now = SystemClock.now()
        simpleWorkouts.filter {
            it.date.start >= now
        }
    }

    private val fullWorkouts by lazy {
        futureSimpleWorkouts.map { simpleWorkout ->
            FullWorkout(
                simpleWorkout = simpleWorkout,
                partner = simplePartnersById[simpleWorkout.partnerId]!!
            )
        }
    }

    private val fullWorkoutsById by lazy {
        fullWorkouts.associateBy { it.id }
    }

    // avoid JVM name clash ;-)
    private val futureFullWorkoutss by lazy {
        val now = SystemClock.now()
        fullWorkouts.filter { it.date.start > now }
    }

    private val fullPartnersById by lazy {
        val now = SystemClock.now()
        simplePartners.map { simplePartner ->
            FullPartner(
                simplePartner = simplePartner,
                pastWorkouts = simpleWorkouts.filter { it.partnerId == simplePartner.id && it.date.start <= now },
                currentWorkouts = simpleWorkouts.filter { it.partnerId == simplePartner.id && it.date.start > now },
            )
        }.associateBy { it.id }
    }

    override fun getAllCategories(): List<String> =
        ExposedCategoriesRepo.selectAll().map { it.name }

    override fun getFutureFullWorkouts() =
        futureFullWorkoutss

    override fun getFullPartnerById(partnerId: Int): FullPartner =
        fullPartnersById[partnerId] ?: error("Could not find partner by ID: $partnerId")

    override fun getFullWorkoutById(workoutId: Int): FullWorkout =
        fullWorkoutsById[workoutId] ?: error("Could not find workout by ID: $workoutId")

    override fun updatePartner(modifications: PartnerModifications) {
        ExposedPartnersRepo.update(modifications)
    }
}

private fun PartnerEntity.toSimplePartner(
    image: Image,
    categories: List<String>,
    url: String,
    checkins: Int,
) = SimplePartner(
    image = image,
    categories = categories,
    url = url,
    checkins = checkins,
    id = id,
    name = name,
    note = note,
    description = description,
    facilities = facilities,
    rating = rating,
    isFavorited = isFavorited,
    isWishlisted = isWishlisted,
    isHidden = isHidden,
)

private fun WorkoutEntity.toSimpleWorkout(isReserved: Boolean, image: Image) = SimpleWorkout(
    id = id,
    partnerId = partnerId,
    name = name,
    about = about,
    specifics = specifics,
    address = address,
    date = DateRange(start = start.fromUtcToAmsterdamZonedDateTime(), end = end.fromUtcToAmsterdamZonedDateTime()),
    image = image,
    url = OnefitUtils.workoutUrl(id, slug),
    isReserved = isReserved,
)
