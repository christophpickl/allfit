package allfit.sync

import allfit.api.OnefitClient
import allfit.api.PartnerSearchParams
import allfit.api.WorkoutSearchParams
import allfit.api.models.CategoriesJson
import allfit.api.models.CategoryJsonDefinition
import allfit.api.models.PartnerCategoryJson
import allfit.api.models.PartnerJson
import allfit.api.models.PartnersJson
import allfit.api.models.SyncableJson
import allfit.api.models.WorkoutJson
import allfit.domain.HasIntId
import allfit.persistence.BaseRepo
import allfit.persistence.CategoriesRepo
import allfit.persistence.CategoryEntity
import allfit.persistence.PartnerEntity
import allfit.persistence.PartnersRepo
import allfit.persistence.WorkoutEntity
import allfit.persistence.WorkoutsRepo
import allfit.service.SystemClock
import allfit.service.toUtcLocalDateTime
import mu.KotlinLogging.logger

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
    private val workoutsRepo: WorkoutsRepo,
) : Syncer {

    private val log = logger {}

    override suspend fun syncAll() {
        log.info { "Sync started ..." }
        val partners = client.getPartners(PartnerSearchParams.simple())
        syncAny(categoriesRepo, mergedCategories(client.getCategories(), partners)) { it.toCategoryEntity() }
        syncAny(partnersRepo, partners.data) { it.toPartnerEntity() }
        syncWorkouts()
    }

    private suspend fun syncWorkouts() {
        val from = SystemClock.todayBeginOfDay()
        val workouts = client.getWorkouts(
            WorkoutSearchParams.simple(
                from = from,
                plusDays = 14
            )
        ).data

        workoutsRepo.insertAll(workouts.map { it.toWorkoutEntity() })
        // FIXME calculate diff of workouts! only insert what is not yet existing
//        val partnersById = partnersRepo.selectAll().associateBy { it.id }
//        workoutsRepo.insert(workouts.map { it.toWorkout(partnersById.findOrThrow(it.partner.id)) })
        // FIXME delete workouts before today which has no association with a reservation.
    }
}

private fun mergedCategories(categories: CategoriesJson, partners: PartnersJson) =
    mutableMapOf<Int, CategoryJsonDefinition>().apply {
        putAll(categories.data.associateBy { it.id })
        putAll(partners.toFlattenedCategories().associateBy { it.id })
    }.values.toList()

private fun <
        REPO : BaseRepo<ENTITY>,
        ENTITY : HasIntId,
        JSON : SyncableJson
        > syncAny(repo: REPO, syncableJsons: List<JSON>, mapper: (JSON) -> ENTITY) {
    val localDomains = repo.selectAll()
    val report = Differ.diff(localDomains, syncableJsons, mapper)

    if (report.toInsert.isNotEmpty()) {
        repo.insertAll(report.toInsert)
    }
    if (report.toDelete.isNotEmpty()) {
        repo.deleteAll(report.toDelete.map { it.id })
    }
}

private fun PartnersJson.toFlattenedCategories() = data.map { partner ->
    mutableListOf<PartnerCategoryJson>().also {
        it.add(partner.category)
        it.addAll(partner.categories)
    }
}.flatten()

fun CategoryJsonDefinition.toCategoryEntity() = CategoryEntity(
    id = id,
    name = name,
    isDeleted = false,
)

private fun PartnerJson.toPartnerEntity() = PartnerEntity(
    id = id,
    name = name,
    categoryIds = mutableListOf<Int>().apply {
        add(category.id)
        addAll(categories.map { it.id })
    },
    isDeleted = false,
)

private fun WorkoutJson.toWorkoutEntity() = WorkoutEntity(
    id = id,
    name = name,
    slug = slug,
    start = from.toUtcLocalDateTime(),
    end = till.toUtcLocalDateTime(),
    partnerId = partner.id
)
