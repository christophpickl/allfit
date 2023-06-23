package allfit.presentation.logic

import allfit.api.OnefitUtils
import allfit.persistence.domain.CategoriesRepo
import allfit.persistence.domain.CheckinEntity
import allfit.persistence.domain.CheckinType
import allfit.persistence.domain.CheckinsRepository
import allfit.persistence.domain.PartnerEntity
import allfit.persistence.domain.PartnersRepo
import allfit.persistence.domain.ReservationsRepo
import allfit.persistence.domain.WorkoutEntity
import allfit.persistence.domain.WorkoutsRepo
import allfit.presentation.PartnerModifications
import allfit.presentation.models.Checkin
import allfit.presentation.models.DateRange
import allfit.presentation.models.FullPartner
import allfit.presentation.models.FullWorkout
import allfit.presentation.models.HIDDEN_IMAGE
import allfit.presentation.models.NOT_HIDDEN_IMAGE
import allfit.presentation.models.SimplePartner
import allfit.presentation.models.SimpleWorkout
import allfit.service.Clock
import allfit.service.ImageStorage
import allfit.service.Images
import allfit.service.beginOfDay
import allfit.service.fromUtcToAmsterdamZonedDateTime
import javafx.scene.image.Image
import mu.KotlinLogging.logger

interface DataStorage {
    fun getCategories(): List<String>
    fun getPartners(): List<FullPartner>
    fun getPartnerById(partnerId: Int): FullPartner
    fun getWorkoutById(workoutId: Int): FullWorkout

    /* past visited ones, or upcoming ones */
    fun getWorkouts(): List<FullWorkout>
    fun updatePartner(modifications: PartnerModifications)
    fun hidePartner(partnerId: Int)
    fun unhidePartner(partnerId: Int)
}

class ExposedDataStorage(
    private val categoriesRepo: CategoriesRepo,
    private val reservationsRepo: ReservationsRepo,
    private val checkinsRepository: CheckinsRepository,
    private val partnersRepo: PartnersRepo,
    private val workoutsRepo: WorkoutsRepo,
    private val imageStorage: ImageStorage,
    private val clock: Clock,
) : DataStorage {

    private val log = logger {}

    private val simplePartners by lazy {
        val categoriesById = categoriesRepo.selectAll().associateBy { it.id }
        val partnerEntities = partnersRepo.selectAll()
        val partnerImagesByPartnerId = imageStorage.loadPartnerImages(partnerEntities.map { it.id })
            .associateBy { it.partnerId }

        val checkinsByPartnerId = checkinsRepository.selectCountForPartners().associateBy { it.partnerId }

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
    private val simpleWorkouts by lazy {
        val workoutsWithReservation = reservationsRepo.selectAll().map { it.workoutId }.toSet()
        val workoutsWithCheckins = checkins.mapNotNull { it.workoutId }.toSet()
        val storedWorkouts = workoutsRepo.selectAll()
        val workoutImages = imageStorage.loadWorkoutImages(storedWorkouts.map { it.id }).associateBy { it.workoutId }
        storedWorkouts.map { workoutEntity ->
            try {
                workoutEntity.fromDropinToSimpleWorkout(
                    image = Image(workoutImages[workoutEntity.id]!!.inputStream()),
                    isReserved = workoutsWithReservation.contains(workoutEntity.id),
                    wasVisited = workoutsWithCheckins.contains(workoutEntity.id),
                )
            } catch (e: Exception) {
                log.error("Corrupt workout with ID: ${workoutEntity.id}")
                throw e
            }
        }
    }

    private val simplePartnersById by lazy {
        simplePartners.associateBy { it.id }
    }

    private val fullWorkoutsVisitedOrUpcomingOrDropins by lazy {
        val now = clock.now().beginOfDay()
        simpleWorkouts
            .filter { it.wasVisited || it.date.start >= now }
            .map { simpleWorkout ->
                FullWorkout(
                    simpleWorkout = simpleWorkout,
                    partner = simplePartnersById[simpleWorkout.partnerId]!!
                )
            } +
                dropinCheckins.map { checkin ->
                    val partner = simplePartnersById[checkin.partnerId]!!
                    FullWorkout(
                        simpleWorkout = checkin.fromDropinToSimpleWorkout(partner.url),
                        partner = partner
                    )
                }
    }

    private val fullWorkoutsById by lazy {
        fullWorkoutsVisitedOrUpcomingOrDropins.associateBy { it.id }
    }

    private val fullPartners by lazy {
        val now = clock.now()
        simplePartners.map { simplePartner ->
            FullPartner(
                simplePartner = simplePartner,
                pastCheckins = simpleWorkouts.filter {
                    it.partnerId == simplePartner.id &&
                            it.date.start <= now &&
                            visitedWorkoutIds.contains(it.id)
                }.map {
                    Checkin.WorkoutCheckin(it)
                } + dropinCheckins.filter { it.partnerId == simplePartner.id }
                    .map { Checkin.DropinCheckin(it.createdAt.fromUtcToAmsterdamZonedDateTime()) },
                upcomingWorkouts = simpleWorkouts.filter { it.partnerId == simplePartner.id && it.date.start > now },
            )
        }
    }

    private val fullPartnersById by lazy {
        fullPartners.associateBy { it.id }
    }

    private val checkins by lazy {
        checkinsRepository.selectAll()
    }

    private val visitedWorkoutIds by lazy {
        checkins.mapNotNull { it.workoutId }
    }

    private val dropinCheckins by lazy {
        checkins.filter { it.type == CheckinType.DROP_IN }
    }

    override fun getCategories() =
        categoriesRepo.selectAll().map { it.name }.distinct().sorted()

    override fun getPartners() =
        fullPartners

    override fun getWorkouts() =
        fullWorkoutsVisitedOrUpcomingOrDropins

    override fun getPartnerById(partnerId: Int): FullPartner =
        fullPartnersById[partnerId] ?: error("Could not find partner by ID: $partnerId")

    override fun getWorkoutById(workoutId: Int): FullWorkout =
        fullWorkoutsById[workoutId] ?: error("Could not find workout by ID: $workoutId")

    override fun updatePartner(modifications: PartnerModifications) {
        partnersRepo.update(modifications)
        val storedPartner = getPartnerById(modifications.partnerId)
        modifications.update(storedPartner.simplePartner)
    }

    override fun hidePartner(partnerId: Int) {
        partnersRepo.hide(partnerId)
        val partner = getPartnerById(partnerId)
        partner.isHidden = true
        partner.hiddenImage = HIDDEN_IMAGE
    }

    override fun unhidePartner(partnerId: Int) {
        partnersRepo.unhide(partnerId)
        val partner = getPartnerById(partnerId)
        partner.isHidden = false
        partner.hiddenImage = NOT_HIDDEN_IMAGE
    }
}

private var syntheticWorkoutIdCounter = 90_000_000
private fun CheckinEntity.fromDropinToSimpleWorkout(partnerUrl: String): SimpleWorkout {
    require(type == CheckinType.DROP_IN)
    val start = createdAt.fromUtcToAmsterdamZonedDateTime()
    return SimpleWorkout(
        id = syntheticWorkoutIdCounter++,
        partnerId = partnerId,
        name = "Drop-In",
        about = "",
        specifics = "",
        address = "",
        date = DateRange(start = start, end = start.plusHours(1)),
        image = Images.dropin,
        url = partnerUrl,
        isReserved = false,
        wasVisited = true,
    )
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
    hiddenImage = if (isHidden) HIDDEN_IMAGE else NOT_HIDDEN_IMAGE,
)

private fun WorkoutEntity.fromDropinToSimpleWorkout(
    image: Image,
    wasVisited: Boolean,
    isReserved: Boolean,
) = SimpleWorkout(
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
    wasVisited = wasVisited,
)
