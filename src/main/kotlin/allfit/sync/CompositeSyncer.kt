package allfit.sync

import allfit.api.JsonLogFileManager
import allfit.api.OnefitClient
import allfit.api.PartnerSearchParams
import allfit.api.models.PartnersJsonRoot
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.jetbrains.exposed.sql.transactions.transaction

class CompositeSyncer(
    private val client: OnefitClient,
    private val categoriesSyncer: CategoriesSyncer,
    private val partnersSyncer: PartnersSyncer,
    private val locationsSyncer: LocationsSyncer,
    private val workoutsSyncer: WorkoutsSyncer,
    private val reservationsSyncer: ReservationsSyncer,
    private val checkinsSyncer: CheckinsSyncer,
    private val listeners: SyncListenerManager,
    private val jsonLogFileManager: JsonLogFileManager,
) : Syncer, SyncListenerManager by listeners {

    private lateinit var partners: PartnersJsonRoot
    private val log = KotlinLogging.logger {}

    private val syncSteps = listOf(
        SyncStep("Fetching partners") {
            partners = client.getPartners(PartnerSearchParams.simple())
        },
        SyncStep("Syncing categories") {
            categoriesSyncer.sync(partners)
        },
        SyncStep("Syncing partners") {
            partnersSyncer.sync(partners)
        },
        SyncStep("Syncing locations") {
            locationsSyncer.sync(partners)
        },
        SyncStep("Syncing workouts") {
            workoutsSyncer.sync()
        },
        SyncStep("Syncing reservations") {
            reservationsSyncer.sync()
        },
        SyncStep("Syncing checkins") {
            checkinsSyncer.sync()
        },
        SyncStep("Cleanup logs") {
            jsonLogFileManager.deleteOldLogs()
        },
    )

    override fun syncAll() {
        transaction {
            listeners.onSyncStart(syncSteps.map { it.message })
            runBlocking {
                log.info { "Sync started for ${syncSteps.size} steps..." }
                syncSteps.forEachIndexed { index, syncStep ->
                    log.debug { "Executing sync step #${index + 1}: ${syncStep.message}" }
                    syncStep.code()
                    listeners.onSyncStepDone(index)
                }
            }
        }
        listeners.onSyncEnd()
    }
}

private data class SyncStep(
    val message: String,
    val code: suspend () -> Unit,
)