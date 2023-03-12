package allfit.persistence

import allfit.domain.Workout
import allfit.service.fromUtcToAmsterdamZonedDateTime
import allfit.service.toUtcLocalDateTime
import mu.KotlinLogging.logger
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.datetime
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.ZonedDateTime

object WorkoutsTable : IntIdTable("PUBLIC.WORKOUTS", "ID") {
    val name = varchar("NAME", 256)
    val slug = varchar("SLUG", 256)
    val start = datetime("START")
    val end = datetime("end")
    val partner = reference("PARTNER", PartnersTable)
}

class WorkoutDbo(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<WorkoutDbo>(WorkoutsTable)

    var name by WorkoutsTable.name
    var slug by WorkoutsTable.slug
    var start by WorkoutsTable.start
    var end by WorkoutsTable.end
    var partner by PartnerDbo referencedOn WorkoutsTable.partner
}

interface WorkoutsRepo {
    fun selectStartingFrom(from: ZonedDateTime): List<Workout>
    fun insert(domainObjects: List<Workout>)
}

class InMemoryWorkoutsRepo : WorkoutsRepo {

    private val log = logger {}
    private val workouts = mutableMapOf<Int, Workout>()

    override fun selectStartingFrom(from: ZonedDateTime) =
        workouts.values.toList().filter { it.start >= from }

    override fun insert(domainObjects: List<Workout>) {
        log.debug { "Inserting ${domainObjects.size} workouts." }
        domainObjects.forEach {
            workouts[it.id] = it
        }
    }
}

object ExposedWorkoutsRepo : WorkoutsRepo {

    private val log = logger {}

    override fun selectStartingFrom(from: ZonedDateTime) = transaction {
        log.debug { "Loading workouts." }
        WorkoutDbo.find { WorkoutsTable.start greaterEq from.toUtcLocalDateTime() }.map { it.toWorkout() }
    }

    override fun insert(domainObjects: List<Workout>) {
        transaction {
            log.debug { "Inserting ${domainObjects.size} workouts." }
            val partnerDbos = PartnersTable.selectByIds(domainObjects.toDistinctPartnerIds())
            domainObjects.forEach { workout ->
                WorkoutDbo.new(workout.id) {
                    name = workout.name
                    slug = workout.slug
                    start = workout.start.toUtcLocalDateTime()
                    end = workout.end.toUtcLocalDateTime()
                    partner = partnerDbos.findOrThrow(workout.partner.id)
                }
            }
        }
    }
}

private fun List<Workout>.toDistinctPartnerIds() =
    map { it.partner.id }.distinct().map { EntityID(it, PartnersTable) }


private fun WorkoutDbo.toWorkout() = Workout(
    id = id.value,
    name = name,
    slug = slug,
    start = start.fromUtcToAmsterdamZonedDateTime(),
    end = end.fromUtcToAmsterdamZonedDateTime(),
    partner = partner.toPartner()
)
