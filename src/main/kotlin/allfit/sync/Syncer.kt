package allfit.sync

import allfit.api.OnefitClient
import allfit.api.PartnerSearchParams
import allfit.api.WorkoutSearchParams
import allfit.api.models.CategoriesJson
import allfit.api.models.CategoryJsonDefinition
import allfit.api.models.PartnerJson
import allfit.api.models.PartnerLocationJson
import allfit.api.models.PartnersJson
import allfit.api.models.ReservationJson
import allfit.api.models.SyncableJson
import allfit.api.models.WorkoutJson
import allfit.domain.HasIntId
import allfit.persistence.BaseRepo
import allfit.persistence.CategoriesRepo
import allfit.persistence.CategoryEntity
import allfit.persistence.LocationEntity
import allfit.persistence.LocationsRepo
import allfit.persistence.PartnerEntity
import allfit.persistence.PartnersRepo
import allfit.persistence.ReservationEntity
import allfit.persistence.ReservationsRepo
import allfit.persistence.WorkoutEntity
import allfit.persistence.WorkoutsRepo
import allfit.service.SystemClock
import allfit.service.toUtcLocalDateTime
import mu.KotlinLogging.logger
import java.util.UUID

interface Syncer {
    suspend fun syncAll()
}

object NoOpSyncer : Syncer {
    private val log = logger {}
    override suspend fun syncAll() {
        log.info { "No-op syncer is not doing anything." }
    }
}

class RealSyncer(
    private val client: OnefitClient,
    private val categoriesRepo: CategoriesRepo,
    private val partnersRepo: PartnersRepo,
    private val locationsRepo: LocationsRepo,
    private val workoutsRepo: WorkoutsRepo,
    private val reservationsRepo: ReservationsRepo,
) : Syncer {

    private val log = logger {}

    override suspend fun syncAll() {
        log.info { "Sync started ..." }
        val partners = client.getPartners(PartnerSearchParams.simple())
        syncLocations(partners)
        syncAny(
            "categories",
            categoriesRepo,
            mergedCategories(client.getCategories(), partners)
        ) { it.toCategoryEntity() }
        syncAny("partners", partnersRepo, partners.data) { it.toPartnerEntity() }
        syncWorkouts()
        syncReservations()
    }

    private fun syncLocations(partners: PartnersJson) {
        log.debug { "Syncing locations..." }
        val locations = partners.data.map { partner ->
            partner.location_groups.map { it.locations }.flatten()
        }.flatten()
        locationsRepo.insertAll(locations.map { it.toLocationEntity() })
    }

    private suspend fun syncWorkouts() {
        log.debug { "Syncing workouts..." }
        workoutsRepo.insertAll(getWorkoutsToBeSynced().map { it.toWorkoutEntity() })
        // FIXME delete workouts before today which has no association with a reservation.
    }

    private suspend fun getWorkoutsToBeSynced(): List<WorkoutJson> {
        val from = SystemClock.todayBeginOfDay()
        val workouts = client.getWorkouts(WorkoutSearchParams.simple(from = from, plusDays = 14)).data
        val workoutIdsToBeInserted = workouts.map { it.id }.toMutableList()
        val entities = workoutsRepo.selectAllStartingFrom(from.toUtcLocalDateTime())
        entities.forEach {
            workoutIdsToBeInserted.remove(it.id)
        }
        val maybeInsert = workouts.filter { workoutIdsToBeInserted.contains(it.id) }

        // remove all workouts without an existing partner (partner seems disabled, yet workout is being returned)
        val partnerIds = partnersRepo.selectAll().map { it.id }
        return maybeInsert.filter {
            val existing = partnerIds.contains(it.partner.id)
            if (!existing) {
                log.warn { "Dropping workout because partner is not known (set inactive by OneFit?!): $it" }
            }
            existing
        }
    }

    private suspend fun syncReservations() {
        log.debug { "Syncing reservations..." }
        val reservationsRemote = client.getReservations()
        val reservationsLocal = reservationsRepo.selectAllStartingFrom(SystemClock.now().toUtcLocalDateTime())

        val toBeInserted = reservationsRemote.data.associateBy { UUID.fromString(it.uuid) }.toMutableMap()
        reservationsLocal.forEach {
            toBeInserted.remove(it.uuid)
        }

        val toBeDeleted = reservationsLocal.associateBy { it.uuid.toString() }.toMutableMap()
        reservationsRemote.data.forEach {
            toBeDeleted.remove(it.uuid)
        }

        reservationsRepo.insertAll(toBeInserted.values.map { it.toReservationEntity() })
        reservationsRepo.deleteAll(toBeDeleted.map { UUID.fromString(it.key) })
    }

    private fun <
            REPO : BaseRepo<ENTITY>,
            ENTITY : HasIntId,
            JSON : SyncableJson
            > syncAny(label: String, repo: REPO, syncableJsons: List<JSON>, mapper: (JSON) -> ENTITY) {
        log.debug { "Syncing $label ..." }
        val localDomains = repo.selectAll()
        val report = Differ.diff(localDomains, syncableJsons, mapper)

        if (report.toInsert.isNotEmpty()) {
            repo.insertAll(report.toInsert)
        }
        if (report.toDelete.isNotEmpty()) {
            repo.deleteAll(report.toDelete.map { it.id })
        }
    }
}

private fun PartnerLocationJson.toLocationEntity() = LocationEntity(
    id = id.toIntOrNull() ?: error("Invalid, non-numeric location ID '$id'!"),
    partnerId = partner_id,
    streetName = street_name,
    houseNumber = house_number,
    addition = addition,
    zipCode = zip_code,
    city = city,
    latitude = latitude,
    longitude = longitude,
)

private fun ReservationJson.toReservationEntity() = ReservationEntity(
    uuid = UUID.fromString(uuid),
    workoutId = workout.id,
    workoutStart = workout.from.toUtcLocalDateTime(),
)

private fun mergedCategories(categories: CategoriesJson, partners: PartnersJson) =
    mutableMapOf<Int, CategoryJsonDefinition>().apply {
        putAll(categories.data.associateBy { it.id })
        putAll(partners.toFlattenedCategories().associateBy { it.id })
    }.values.toList()


private fun PartnersJson.toFlattenedCategories() = data.map { partner ->
    mutableListOf<CategoryJsonDefinition>().also {
        it.add(partner.category)
        it.addAll(partner.categories)
    }
}.flatten()

fun CategoryJsonDefinition.toCategoryEntity() = CategoryEntity(
    id = id,
    name = name,
    isDeleted = false,
    slug = slugs?.en,
)

private fun PartnerJson.toPartnerEntity() = PartnerEntity(
    id = id,
    categoryIds = mutableListOf<Int>().apply {
        add(category.id)
        addAll(categories.map { it.id })
    }.distinct(), // OneFit sends corrupt data :-/
    name = name,
    slug = slug,
    description = description,
    note = "",
    facilities = facilities.joinToString(","),
    isDeleted = false,
    isFavorited = false,
    isHidden = false,
    isStarred = false,
)

private fun WorkoutJson.toWorkoutEntity() = WorkoutEntity(
    id = id,
    name = name,
    slug = slug,
    start = from.toUtcLocalDateTime(),
    end = till.toUtcLocalDateTime(),
    partnerId = partner.id
)
