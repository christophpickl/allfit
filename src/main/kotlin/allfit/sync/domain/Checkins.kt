package allfit.sync.domain

import allfit.api.CheckinSearchParams
import allfit.api.OnefitClient
import allfit.api.models.CheckinJson
import allfit.api.models.PartnerWorkoutCheckinJson
import allfit.api.models.WorkoutCheckinJson
import allfit.persistence.domain.CategoriesRepo
import allfit.persistence.domain.CheckinEntity
import allfit.persistence.domain.CheckinType
import allfit.persistence.domain.CheckinsRepository
import allfit.persistence.domain.PartnerEntity
import allfit.persistence.domain.PartnersRepo
import allfit.persistence.domain.WorkoutEntity
import allfit.persistence.domain.WorkoutsRepo
import allfit.service.ImageStorage
import allfit.service.toUtcLocalDateTime
import java.util.UUID
import mu.KotlinLogging.logger

interface CheckinsSyncer {
    suspend fun sync()
}

class CheckinsSyncerImpl(
    private val client: OnefitClient,
    private val checkinsRepo: CheckinsRepository,
    private val workoutsRepo: WorkoutsRepo,
    private val partnersRepo: PartnersRepo,
    private val categoriesRepo: CategoriesRepo,
    private val imageStorage: ImageStorage,
) : CheckinsSyncer {

    private val log = logger {}

    override suspend fun sync() {
        log.info { "Syncing checkins." }
        val toBeInserted = checkinsToBeInserted()
        log.debug { "Inserting ${toBeInserted.size} checkins." }
        syncRequirements(toBeInserted)
        checkinsRepo.insertAll(toBeInserted.map { it.toCheckinEntity() })
    }

    private suspend fun checkinsToBeInserted(): List<CheckinJson> {
        val remoteCheckins = client.getCheckins(CheckinSearchParams.simple())
        val localCheckinIds = checkinsRepo.selectAll().map { it.id.toString() }
        return remoteCheckins.data.filter {
            !localCheckinIds.contains(it.uuid)
        }
    }

    private fun syncRequirements(remoteCheckins: List<CheckinJson>) {
        syncCategories(remoteCheckins)
        syncPartners(remoteCheckins)
        syncWorkouts(remoteCheckins)
    }

    private fun syncCategories(remoteCheckins: List<CheckinJson>) {
        val localCategoryIds = categoriesRepo.selectAll().map { it.id }
        val toBeInserted = remoteCheckins.filter {
            !localCategoryIds.contains(it.typeSafePartner.category.id)
        }
        log.debug { "Inserting ${toBeInserted.size} missing categories for checkins." }
        categoriesRepo.insertAll(toBeInserted.map {
            it.typeSafePartner.category.toCategoryEntity()
        })
    }

    private fun syncPartners(remoteCheckins: List<CheckinJson>) {
        val localPartnerIds = partnersRepo.selectAll().map { it.id }
        val toBeInserted = remoteCheckins.filter {
            !localPartnerIds.contains(it.typeSafePartner.id)
        }
        log.debug { "Inserting ${toBeInserted.size} missing partners for checkins." }
        partnersRepo.insertAll(toBeInserted.map {
            it.typeSafePartner.toPartnerEntity()
        })
        imageStorage.saveDefaultImageForPartner(toBeInserted.map { it.typeSafePartner.id })
    }

    private fun syncWorkouts(remoteCheckins: List<CheckinJson>) {
        val remoteWorkoutCheckins = remoteCheckins.filter { it.type == CheckinJson.TYPE_WORKOUT }
        val localWorkoutIds =
            workoutsRepo.selectAllForId(remoteWorkoutCheckins.map { it.workout!!.id })
                .map { it.id }
        val toBeInserted = remoteWorkoutCheckins.filter {
            !localWorkoutIds.contains(it.workout!!.id)
        }
        log.debug { "Inserting ${toBeInserted.size} missing workouts for checkins." }
        workoutsRepo.insertAll(toBeInserted.map {
            it.workout!!.toWorkoutEntity()
        })
        imageStorage.saveDefaultImageForWorkout(toBeInserted.map { it.workout!!.id })
    }
}

private fun PartnerWorkoutCheckinJson.toPartnerEntity() = PartnerEntity(
    id = id,
    primaryCategoryId = category.id,
    secondaryCategoryIds = emptyList(), // when getting partner via checkin, no secondary groups are available
    name = name,
    slug = slug,
    description = "N/A",
    imageUrl = "",
    note = "",
    facilities = "",
    rating = 0,
    isDeleted = false,
    isFavorited = false,
    isWishlisted = false,
    isHidden = false
)

private fun WorkoutCheckinJson.toWorkoutEntity() = WorkoutEntity(
    id = id,
    partnerId = partner.id,
    name = name,
    slug = slug,
    start = from.toUtcLocalDateTime(),
    end = till.toUtcLocalDateTime(),
    about = "N/A",
    specifics = "N/A",
    address = "N/A",
)

private fun CheckinJson.toCheckinEntity() = CheckinEntity(
    id = UUID.fromString(uuid),
    type = when (type) {
        CheckinJson.TYPE_WORKOUT -> CheckinType.WORKOUT
        CheckinJson.TYPE_DROPIN -> CheckinType.DROP_IN
        else -> error("invalid type: $type")
    },
    partnerId = typeSafePartner.id,
    workoutId = workout?.id,
    createdAt = created_at.toUtcLocalDateTime(),
)
