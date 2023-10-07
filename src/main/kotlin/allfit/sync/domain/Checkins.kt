package allfit.sync.domain

import allfit.api.CheckinSearchParams
import allfit.api.OnefitClient
import allfit.api.models.CheckinJson
import allfit.api.models.PartnerWorkoutCheckinJson
import allfit.api.models.WorkoutCheckinJson
import allfit.domain.Location
import allfit.persistence.domain.CategoriesRepo
import allfit.persistence.domain.CheckinEntity
import allfit.persistence.domain.CheckinType
import allfit.persistence.domain.CheckinsRepository
import allfit.persistence.domain.PartnerEntity
import allfit.persistence.domain.PartnersRepo
import allfit.persistence.domain.SinglesRepo
import allfit.persistence.domain.WorkoutEntity
import allfit.persistence.domain.WorkoutsRepo
import allfit.service.ImageStorage
import allfit.service.toUtcLocalDateTime
import io.github.oshai.kotlinlogging.KotlinLogging.logger
import java.util.UUID

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
    private val singlesRepo: SinglesRepo,
) : CheckinsSyncer {

    private val log = logger {}

    override suspend fun sync() {
        log.info { "Syncing checkins." }
        val toBeInserted = checkinsToBeInserted()
        log.debug { "Inserting ${toBeInserted.size} checkins." }
        syncRequirements(singlesRepo.selectLocation(), toBeInserted)
        checkinsRepo.insertAll(toBeInserted.map { it.toCheckinEntity() })
    }

    private suspend fun checkinsToBeInserted(): List<CheckinJson> {
        val remoteCheckins = client.getCheckins(CheckinSearchParams.simple())
        val localCheckinIds = checkinsRepo.selectAll().map { it.id.toString() }
        return remoteCheckins.filter {
            !localCheckinIds.contains(it.uuid)
        }
    }

    private fun syncRequirements(location: Location, remoteCheckins: List<CheckinJson>) {
        syncCategories(remoteCheckins)
        syncPartners(location, remoteCheckins)
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

    private fun syncPartners(location: Location, remoteCheckins: List<CheckinJson>) {
        val localPartnerIds = partnersRepo.selectAllIds()
        val partnersToInsert = remoteCheckins.filter { !localPartnerIds.contains(it.typeSafePartner.id) }
            .distinctBy { it.typeSafePartner.id }
            .map { it.typeSafePartner.toPartnerEntity(location) }

        log.debug { "Inserting ${partnersToInsert.size} missing partners for checkins." }
        partnersRepo.insertAll(partnersToInsert)
        imageStorage.saveDefaultImageForPartner(partnersToInsert.map { it.id })
    }

    private fun syncWorkouts(remoteCheckins: List<CheckinJson>) {
        val remoteWorkoutCheckins = remoteCheckins.filter { it.type == CheckinJson.TYPE_WORKOUT }
        val localWorkoutIds =
            workoutsRepo.selectAllForIds(remoteWorkoutCheckins.map { it.workout!!.id })
                .map { it.id }
        val toBeInserted = remoteWorkoutCheckins.filter {
            !localWorkoutIds.contains(it.workout!!.id)
        }
        log.debug { "Inserting ${toBeInserted.size} missing workouts for checkins." }
        workoutsRepo.insertAll(toBeInserted.map {
            it.workout!!.toWorkoutEntity()
        })
    }
}

private fun PartnerWorkoutCheckinJson.toPartnerEntity(location: Location) = PartnerEntity(
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
    isHidden = false,
    locationShortCode = location.shortCode,
    hasWorkouts = null,
    hasDropins = null,
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
    teacher = null,
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
