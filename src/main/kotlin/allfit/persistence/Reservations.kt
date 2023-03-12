package allfit.persistence

import mu.KotlinLogging.logger
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.javatime.datetime
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime
import java.util.UUID

object ReservationsTable : Table("PUBLIC.RESERVATIONS") {
    val uuid = uuid("ID")
    val workoutStart = datetime("START")
    val workoutId = reference("WORKOUT_ID", WorkoutsTable)
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
}

class InMemoryReservationsRepo : ReservationsRepo {

    private val log = logger {}
    private val reservations = mutableMapOf<UUID, ReservationEntity>()

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

}

object ExposedReservationsRepo : ReservationsRepo {

    private val log = logger {}

    override fun selectAll(): List<ReservationEntity> = transaction {
        log.debug { "Selecting all reservations." }
        ReservationsTable.selectAll().map { it.toReservationsEntity() }
    }

    override fun selectAllStartingFrom(fromInclusive: LocalDateTime): List<ReservationEntity> = transaction {
        log.debug { "Selecting reservations from: $fromInclusive" }
        ReservationsTable.select {
            ReservationsTable.workoutStart greaterEq fromInclusive
        }.map { it.toReservationsEntity() }
    }

    override fun insertAll(reservations: List<ReservationEntity>) {
        transaction {
            log.debug { "Inserting ${reservations.size} reservations." }
            reservations.forEach { reservation ->
                ReservationsTable.insert {
                    it[uuid] = reservation.uuid
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
                uuid inList uuids
            }
        }
    }
}

private fun ResultRow.toReservationsEntity() = ReservationEntity(
    uuid = this[ReservationsTable.uuid],
    workoutId = this[ReservationsTable.workoutId].value,
    workoutStart = this[ReservationsTable.workoutStart],
)
