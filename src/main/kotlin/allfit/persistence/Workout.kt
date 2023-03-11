package allfit.persistence

import allfit.domain.Workout
import mu.KotlinLogging.logger
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.transactions.transaction

object WorkoutsTable : IntIdTable("PUBLIC.WORKOUTS", "ID") {
    val name = varchar("NAME", 256)
    val slug = varchar("SLUG", 256)
    val partner = reference("PARTNER", PartnersTable)
}

class WorkoutDbo(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<WorkoutDbo>(WorkoutsTable)

    var name by WorkoutsTable.name
    var slug by WorkoutsTable.slug
    var partner by PartnerDbo referencedOn WorkoutsTable.partner
}

interface WorkoutsRepo {
    fun select(): List<Workout>
    fun insert(domainObjects: List<Workout>)
}

class InMemoryWorkoutsRepo : WorkoutsRepo {

    private val log = logger {}
    private val workouts = mutableMapOf<Int, Workout>()

    override fun select() = workouts.values.toList()

    override fun insert(domainObjects: List<Workout>) {
        log.debug { "Inserting ${domainObjects.size} workouts." }
        domainObjects.forEach {
            workouts[it.id] = it
        }
    }
}

object ExposedWorkoutsRepo : WorkoutsRepo {

    private val log = logger {}

    override fun select() = transaction {
        log.debug { "Loading workouts." }
        WorkoutDbo.all().map { it.toWorkout() }
    }

    override fun insert(domainObjects: List<Workout>) {
        transaction {
            log.debug { "Inserting ${domainObjects.size} workouts." }
            val partnerDbos = PartnersTable.selectByIds(domainObjects.toDistinctPartnerIds())
            domainObjects.forEach { workout ->
                WorkoutDbo.new(workout.id) {
                    name = workout.name
                    slug = workout.slug
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
    partner = partner.toPartner()
)
