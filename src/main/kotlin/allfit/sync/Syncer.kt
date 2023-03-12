package allfit.sync

import allfit.api.OnefitClient
import allfit.api.PartnerSearchParams
import allfit.api.WorkoutSearchParams
import allfit.api.models.CategoriesJson
import allfit.api.models.CategoryJsonDefinition
import allfit.api.models.PartnerCategoryJson
import allfit.api.models.PartnerJson
import allfit.api.models.PartnersJson
import allfit.api.models.SyncableJsonEntity
import allfit.api.models.WorkoutJson
import allfit.domain.Category
import allfit.domain.HasIntId
import allfit.domain.Partner
import allfit.domain.Workout
import allfit.domain.findOrThrow
import allfit.persistence.CategoriesRepo
import allfit.persistence.PartnersRepo
import allfit.persistence.Repo
import allfit.persistence.WorkoutsRepo
import allfit.service.SystemClock
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
        syncAny(categoriesRepo, mergedCategories(client.getCategories(), partners)) { it.toCategory() }
        syncAny(partnersRepo, partners.data) { it.toPartner() }
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

        // FIXME calculate diff of workouts! only insert what is not yet existing
        val partnersById = partnersRepo.select().associateBy { it.id }
        workoutsRepo.insert(workouts.map { it.toWorkout(partnersById.findOrThrow(it.partner.id)) })
        // FIXME delete workouts before today which has no association with a reservation.
    }
}

private fun WorkoutJson.toWorkout(partner: Partner) = Workout(
    id = id,
    name = name,
    slug = slug,
    start = from,
    end = till,
    partner = partner
)

private fun mergedCategories(categories: CategoriesJson, partners: PartnersJson) =
    mutableMapOf<Int, CategoryJsonDefinition>().apply {
        putAll(categories.data.associateBy { it.id })
        putAll(partners.toFlattenedCategories().associateBy { it.id })
    }.values.toList()

private fun <
        REPO : Repo<DOMAIN>,
        DOMAIN : HasIntId,
        ENTITY : SyncableJsonEntity
        > syncAny(repo: REPO, syncableJsons: List<ENTITY>, mapper: (ENTITY) -> DOMAIN) {
    val localDomains = repo.select()
    val report = Differ.diff(localDomains, syncableJsons, mapper)

    if (report.toInsert.isNotEmpty()) {
        repo.insert(report.toInsert)
    }
    if (report.toDelete.isNotEmpty()) {
        repo.delete(report.toDelete.map { it.id })
    }
}

private fun PartnersJson.toFlattenedCategories() = data.map { partner ->
    mutableListOf<PartnerCategoryJson>().also {
        it.add(partner.category)
        it.addAll(partner.categories)
    }
}.flatten()

private fun PartnerJson.toPartner() =
    Partner(
        id = id,
        name = name,
        isDeleted = false,
        categories = categories.map { it.toCategory() },
    )

fun CategoryJsonDefinition.toCategory() =
    Category(
        id = id,
        name = name,
        isDeleted = false,
    )
