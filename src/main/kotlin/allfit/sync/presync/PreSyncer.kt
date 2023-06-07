package allfit.sync.presync

import allfit.api.OnefitClient
import allfit.api.models.UsageJson
import allfit.api.models.UsageProductJson
import allfit.api.models.UsageProductRuleJson
import allfit.persistence.domain.UsageEntity
import allfit.persistence.domain.UsageRepository
import allfit.service.toUtcLocalDateTime
import kotlinx.coroutines.runBlocking

interface PreSyncer {
    fun registerListener(listener: PreSyncListener)
    fun sync()
}

class NoOpPreSyncer(
    private val listeners: PreSyncListenerManager
) : PreSyncer, PreSyncListenerManager by listeners {
    override fun sync() {
        listeners.onSyncStart(listOf("Dummy"))
        Thread.sleep(500)
        listeners.onSyncStepDone(0)
        Thread.sleep(500)
        listeners.onSyncEnd()
    }
}

class ApiPreSyncer(
    private val onefitClient: OnefitClient,
    private val listeners: PreSyncListenerManager,
    private val usageRepo: UsageRepository,
) : PreSyncer, PreSyncListenerManager by listeners {
    override fun sync() {
        runBlocking {
            listeners.onSyncStart(listOf("Sync usage"))
            syncUsage()
            listeners.onSyncStepDone(0)
            listeners.onSyncEnd()
        }
    }

    private suspend fun syncUsage() {
        val usage = onefitClient.getUsage()
        usageRepo.upsert(usage.data.toUsageEntity())
    }
}

private fun UsageJson.toUsageEntity() = UsageEntity(
    total = total,
    noShows = no_shows,
    from = period.display_from.toUtcLocalDateTime(),
    until = period.display_till.toUtcLocalDateTime(),
    periodCap = period.product.findByType(UsageProductRuleJson.Types.PERIOD_CAP),
    maxCheckInsOrReservationsPerPeriod = period.product.findByType(UsageProductRuleJson.Types.MAX_PER_PERIOD),
    totalCheckInsOrReservationsPerDay = period.product.findByType(UsageProductRuleJson.Types.TOTAL_PER_DAY),
    maxReservations = period.product.findByType(UsageProductRuleJson.Types.MAX_RESERVATIONS),
)

private fun UsageProductJson.findByType(type: String): Int =
    rules.first { it.type == type }.amount
