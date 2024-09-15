package allfit.persistence.domain

import allfit.persistence.selectSingleton
import allfit.persistence.upsert
import io.github.oshai.kotlinlogging.KotlinLogging.logger
import java.time.LocalDateTime
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime
import org.jetbrains.exposed.sql.transactions.transaction

interface UsageRepository {
    fun upsert(usage: UsageEntity)
    fun selectOne(): UsageEntity
}

class InMemoryUsageRepository : UsageRepository {

    var storedUsage: UsageEntity? = null

    override fun upsert(usage: UsageEntity) {
        storedUsage = usage
    }

    override fun selectOne(): UsageEntity =
        storedUsage!!
}

class MockDateAwareUsageRepository(
    private val delegate: UsageRepository,
    private val mockDate: LocalDateTime?
) : UsageRepository {

    override fun upsert(usage: UsageEntity) {
        delegate.upsert(usage)
    }

    override fun selectOne(): UsageEntity =
        delegate.selectOne().let {
            if (mockDate == null) it
            else it.copy(from = mockDate.withDayOfMonth(1), until = mockDate.withDayOfMonth(27))
        }

}

object ExposedUsageRepo : UsageRepository {

    private val log = logger {}

    override fun upsert(usage: UsageEntity): Unit = transaction {
        log.debug { "upsert(usage=$usage)" }
        @Suppress("DuplicatedCode")
        UsageTable.upsert {
            it[total] = usage.total
            it[noShows] = usage.noShows
            it[from] = usage.from
            it[until] = usage.until
            it[periodCap] = usage.periodCap
            it[maxCheckInsOrReservationsPerPeriod] = usage.maxCheckInsOrReservationsPerPeriod
            it[totalCheckInsOrReservationsPerDay] = usage.totalCheckInsOrReservationsPerDay
            it[maxReservations] = usage.maxReservations
        }
    }

    override fun selectOne(): UsageEntity = transaction {
        UsageTable.selectSingleton().toUsageEntity().also {
            log.debug { "selectOne() = $it" }
        }
    }
}


private fun ResultRow.toUsageEntity() = UsageEntity(
    total = this[UsageTable.total],
    noShows = this[UsageTable.noShows],
    from = this[UsageTable.from],
    until = this[UsageTable.until],
    periodCap = this[UsageTable.periodCap],
    maxCheckInsOrReservationsPerPeriod = this[UsageTable.maxCheckInsOrReservationsPerPeriod],
    totalCheckInsOrReservationsPerDay = this[UsageTable.totalCheckInsOrReservationsPerDay],
    maxReservations = this[UsageTable.maxReservations],
)

object UsageTable : Table("PUBLIC.USAGE") {
    val total = integer("TOTAL")
    val noShows = integer("NO_SHOWS")
    val from = datetime("FROM")
    val until = datetime("UNTIL")
    val periodCap = integer("PERIOD")
    val maxCheckInsOrReservationsPerPeriod = integer("MAX_PER_PERIOD")
    val totalCheckInsOrReservationsPerDay = integer("TOTAL_PER_DAY")
    val maxReservations = integer("MAX_RESERVATIONS")
}

data class UsageEntity(
    val total: Int,
    val noShows: Int,
    val from: LocalDateTime,
    val until: LocalDateTime,
    val periodCap: Int, // 16
    val maxCheckInsOrReservationsPerPeriod: Int, // 4
    val totalCheckInsOrReservationsPerDay: Int, // 2
    val maxReservations: Int, // 6
)
