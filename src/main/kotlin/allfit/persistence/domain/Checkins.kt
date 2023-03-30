package allfit.persistence.domain

import mu.KotlinLogging.logger
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.javatime.datetime
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime
import java.util.UUID

interface CheckinsRepository {
    fun selectAll(): List<CheckinEntity>
    fun insertAll(checkins: List<CheckinEntity>)
}

object CheckinsTable : Table("PUBLIC.CHECKINS") {
    val uuid = varchar("UUID", 36)
    val workoutId = reference("WORKOUT_ID", WorkoutsTable)
    val createdAt = datetime("CREATED_AT")
    override val primaryKey = PrimaryKey(uuid, name = "PK_CHECKINS")
}

data class CheckinEntity(
    val id: UUID,
    val workoutId: Int,
    val createdAt: LocalDateTime
)

class InMemoryCheckinsRepository : CheckinsRepository {
    var checkins = mutableListOf<CheckinEntity>()
    override fun selectAll() = checkins
    override fun insertAll(checkins: List<CheckinEntity>) {
        this.checkins.addAll(checkins)
    }
}

object ExposedCheckinsRepository : CheckinsRepository {

    private val log = logger {}

    override fun selectAll(): List<CheckinEntity> = transaction {
        log.debug { "select all." }
        CheckinsTable.selectAll().map { it.toCheckinEntity() }
    }

    override fun insertAll(checkins: List<CheckinEntity>) {
        transaction {
            log.debug { "Inserting ${checkins.size} checkins." }
            checkins.forEach { checkin ->
                CheckinsTable.insert {
                    it[uuid] = checkin.id.toString()
                    it[workoutId] = checkin.workoutId
                    it[createdAt] = checkin.createdAt
                }
            }
        }
    }
}

private fun ResultRow.toCheckinEntity() = CheckinEntity(
    id = UUID.fromString(this[CheckinsTable.uuid]),
    workoutId = this[CheckinsTable.workoutId].value,
    createdAt = this[CheckinsTable.createdAt],
)
