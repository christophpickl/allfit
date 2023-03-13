package allfit.persistence

import mu.KotlinLogging.logger
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.javatime.datetime
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime

object WorkoutsTable : IntIdTable("PUBLIC.WORKOUTS", "ID") {
    val partnerId = reference("PARTNER_ID", PartnersTable)
    val name = varchar("NAME", 256)
    val slug = varchar("SLUG", 256)
    val start = datetime("START")
    val end = datetime("END")
    // we don't store the location of a workout; it must be one of the partner's anyway
}

data class WorkoutEntity(
    val id: Int,
    val name: String,
    val slug: String,
    val start: LocalDateTime,
    val end: LocalDateTime,
    val partnerId: Int,
)

interface WorkoutsRepo {
    fun selectAllStartingFrom(fromInclusive: LocalDateTime): List<WorkoutEntity>
    fun insertAll(workouts: List<WorkoutEntity>)
}

class InMemoryWorkoutsRepo : WorkoutsRepo {

    private val log = logger {}
    private val workouts = mutableMapOf<Int, WorkoutEntity>()

    override fun selectAllStartingFrom(fromInclusive: LocalDateTime) =
        workouts.values.filter { it.start >= fromInclusive }

    override fun insertAll(workouts: List<WorkoutEntity>) {
        log.debug { "Inserting ${workouts.size} workouts." }
        workouts.forEach {
            this.workouts[it.id] = it
        }
    }
}

object ExposedWorkoutsRepo : WorkoutsRepo {

    private val log = logger {}

    override fun selectAllStartingFrom(fromInclusive: LocalDateTime) = transaction {
        log.debug { "Selecting workouts from: $fromInclusive" }
        WorkoutsTable.select {
            WorkoutsTable.start greaterEq fromInclusive
        }.map { it.toWorkoutEntity() }
    }

    override fun insertAll(workouts: List<WorkoutEntity>) {
        transaction {
            log.debug { "Inserting ${workouts.size} workouts." }
            workouts.forEach { workout ->
                WorkoutsTable.insert {
                    it[WorkoutsTable.id] = workout.id
                    it[name] = workout.name
                    it[slug] = workout.slug
                    it[start] = workout.start
                    it[end] = workout.end
                    it[partnerId] = EntityID(workout.partnerId, PartnersTable)
                }
            }
        }
    }
}

private fun ResultRow.toWorkoutEntity() = WorkoutEntity(
    id = this[WorkoutsTable.id].value,
    name = this[WorkoutsTable.name],
    slug = this[WorkoutsTable.slug],
    start = this[WorkoutsTable.start],
    end = this[WorkoutsTable.end],
    partnerId = this[WorkoutsTable.partnerId].value,
)
