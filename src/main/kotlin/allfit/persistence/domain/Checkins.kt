package allfit.persistence.domain

import io.github.oshai.kotlinlogging.KotlinLogging.logger
import java.time.LocalDateTime
import java.util.UUID
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.alias
import org.jetbrains.exposed.sql.count
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.javatime.datetime
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

interface CheckinsRepository {
    fun selectAll(): List<CheckinEntity>
    fun insertAll(checkins: List<CheckinEntity>)
    fun selectCountForPartners(): List<PartnerAndCheckins>
}

data class PartnerAndCheckins(
    val partnerId: Int,
    val checkinsCount: Int,
)

object CheckinsTable : Table("PUBLIC.CHECKINS") {
    val uuid = varchar("UUID", 36)
    val createdAt = datetime("CREATED_AT")
    val type = enumerationByName<CheckinType>("TYPE", 16)
    val workoutId = reference("WORKOUT_ID", WorkoutsTable).nullable()
    val partnerId = reference("PARTNER_ID", PartnersTable)
    override val primaryKey = PrimaryKey(uuid, name = "PK_CHECKINS")
}

/** CAVE: identifiers/names are used for DB key! */
enum class CheckinType {
    WORKOUT,
    DROP_IN,
}

data class CheckinEntity(
    val id: UUID,
    val type: CheckinType,
    val createdAt: LocalDateTime,
    val partnerId: Int,
    val workoutId: Int?, // only set if type == WORKOUT
)

class InMemoryCheckinsRepository : CheckinsRepository {

    var checkins = mutableListOf<CheckinEntity>()
    var partnerAndCheckins = mutableListOf<PartnerAndCheckins>()

    override fun selectAll() = checkins
    override fun insertAll(checkins: List<CheckinEntity>) {
        this.checkins.addAll(checkins)
    }

    override fun selectCountForPartners() =
        partnerAndCheckins
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
                    it[createdAt] = checkin.createdAt
                    it[type] = checkin.type
                    it[workoutId] = checkin.workoutId
                    it[partnerId] = checkin.partnerId
                }
            }
        }
    }

    override fun selectCountForPartners(): List<PartnerAndCheckins> = transaction {
        log.debug { "selectCountForPartners()" }
        val countColumn = CheckinsTable.partnerId.count().alias("countCheckinsByPartnerId")
        PartnersTable
            .join(CheckinsTable, JoinType.LEFT, onColumn = CheckinsTable.partnerId, otherColumn = PartnersTable.id)
            .slice(PartnersTable.id, countColumn)
            .selectAll()
            .groupBy(PartnersTable.id)
            .map {
                PartnerAndCheckins(
                    partnerId = it[PartnersTable.id].value,
                    checkinsCount = it[countColumn].toInt(),
                )
            }
    }

}

private fun ResultRow.toCheckinEntity() = CheckinEntity(
    id = UUID.fromString(this[CheckinsTable.uuid]),
    createdAt = this[CheckinsTable.createdAt],
    type = this[CheckinsTable.type],
    workoutId = this[CheckinsTable.workoutId]?.value,
    partnerId = this[CheckinsTable.partnerId].value,
)
