package allfit.persistence.domain

import java.time.LocalDateTime
import java.util.UUID
import mu.KotlinLogging.logger
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.SqlExpressionBuilder.less
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.javatime.datetime
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

object ReservationsTable : Table("PUBLIC.RESERVATIONS") {
    val uuid = varchar("ID", 36)
    val workoutId = reference("WORKOUT_ID", WorkoutsTable)
    val workoutStart = datetime("START")
    override val primaryKey = PrimaryKey(uuid)
}

data class ReservationEntity(
    val uuid: UUID,
    val workoutId: Int,
    val workoutStart: LocalDateTime,
)

interface ReservationsRepo {
    fun selectAll(): List<ReservationEntity>
    fun selectAllStartingFrom(fromInclusive: LocalDateTime): List<ReservationEntity>
    fun insertAll(reservations: List<ReservationEntity>)
    fun deleteAll(uuids: List<UUID>)
    fun deleteAllBefore(untilExclusive: LocalDateTime)
}

class InMemoryReservationsRepo : ReservationsRepo {

    private val log = logger {}
    val reservations = mutableMapOf<UUID, ReservationEntity>()

    override fun selectAll() =
        reservations.values.toList()

    override fun selectAllStartingFrom(fromInclusive: LocalDateTime) =
        reservations.values.filter { it.workoutStart >= fromInclusive }

    override fun insertAll(reservations: List<ReservationEntity>) {
        log.debug { "Inserting ${reservations.size} reservations." }
        reservations.forEach {
            this.reservations[it.uuid] = it
        }
    }

    override fun deleteAll(uuids: List<UUID>) {
        log.debug { "Deleting ${reservations.size} reservations." }
        uuids.forEach {
            reservations.remove(it)
        }
    }

    override fun deleteAllBefore(untilExclusive: LocalDateTime) {
        reservations.filter {
            it.value.workoutStart < untilExclusive
        }.forEach { (key, _) ->
            reservations.remove(key)
        }
    }
}

object ExposedReservationsRepo : ReservationsRepo {

    private val log = logger {}

    override fun selectAll(): List<ReservationEntity> = transaction {
        ReservationsTable.selectAll().map { it.toReservationsEntity() }.also {
            log.debug { "Selecting all returns ${it.size} reservations." }
        }
    }

    override fun selectAllStartingFrom(fromInclusive: LocalDateTime): List<ReservationEntity> = transaction {

        ReservationsTable.select {
            ReservationsTable.workoutStart greaterEq fromInclusive
        }.map { it.toReservationsEntity() }.also {
            log.debug { "Selecting from $fromInclusive returns ${it.size} reservations." }
        }
    }

    override fun insertAll(reservations: List<ReservationEntity>) {
        transaction {
            log.debug { "Inserting ${reservations.size} reservations." }
            reservations.forEach { reservation ->
                ReservationsTable.insert {
                    it[uuid] = reservation.uuid.toString()
                    it[workoutStart] = reservation.workoutStart
                    it[workoutId] = reservation.workoutId
                }
            }
        }
    }

    override fun deleteAll(uuids: List<UUID>) {
        transaction {
            log.debug { "Deleting ${uuids.size} reservations." }
            ReservationsTable.deleteWhere {
                uuid inList uuids.map { it.toString() }
            }
        }
    }

    override fun deleteAllBefore(untilExclusive: LocalDateTime) {
        transaction {
            log.debug { "Deleting reservations before: $untilExclusive." }
            ReservationsTable.deleteWhere {
                workoutStart less untilExclusive
            }
        }
    }
}

private fun ResultRow.toReservationsEntity() = ReservationEntity(
    uuid = UUID.fromString(this[ReservationsTable.uuid]),
    workoutId = this[ReservationsTable.workoutId].value,
    workoutStart = this[ReservationsTable.workoutStart],
)
