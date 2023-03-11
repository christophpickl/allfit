package allfit.persistence

import mu.KotlinLogging.logger
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

interface PartnersRepo : Repo<PartnerDbo>

data class PartnerDbo(
    override val id: Int,
    override val isDeleted: Boolean,
    val name: String,
    val categories: List<Int>,
) : Dbo {
    init {
        require(name.length < 256)
    }
}

// TODO better way to do exposed mapping!
//class PartnerDbo2(id: EntityID<Int>): IntEntity(id) {
//    companion object : IntEntityClass<PartnerDbo2>(PartnersTable)
//    var name by PartnersTable.name
//}

object PartnersTable : IntIdTable("PUBLIC.PARTNERS", "ID") {
    val name = varchar("NAME", 256)
    val isDeleted = bool("IS_DELETED")
    // categories n:m
}

class InMemoryPartnersRepo : PartnersRepo {

    private val log = logger {}
    private val partners = mutableListOf<PartnerDbo>()

    override fun select() = partners

    override fun insert(dbos: List<PartnerDbo>) {
        log.debug { "Inserting ${dbos.size} partners." }
        this.partners += dbos
    }

    override fun delete(ids: List<Int>) {
        log.debug { "Deleting ${ids.size} partners." }
        val toDelete = partners.filter { ids.contains(it.id) }
        partners.removeAll(toDelete)
        partners.addAll(toDelete.map {
            it.copy(isDeleted = true)
        })
    }
}

object ExposedPartnersRepo : PartnersRepo {

    private val log = logger {}

    override fun select() = transaction {
        log.debug { "Loading partners." }
        PartnersTable.selectAll().toList().map {
            PartnerDbo(
                id = it[PartnersTable.id].value,
                name = it[PartnersTable.name],
                isDeleted = it[PartnersTable.isDeleted],
                categories = emptyList(), // FIXME
            )
        }
    }

    override fun insert(dbos: List<PartnerDbo>) {
        transaction {
            log.debug { "Inserting ${dbos.size} partners." }
            dbos.forEach { partner ->
                PartnersTable.insert {
                    it[id] = partner.id
                    it[name] = partner.name
                    it[isDeleted] = partner.isDeleted
                }
            }
        }
    }

    override fun delete(ids: List<Int>) {
        transaction {
            log.debug { "Deleting ${ids.size} partners." }
            PartnersTable.update(where = {
                PartnersTable.id inList ids
            }) {
                it[isDeleted] = true
            }
        }
    }
}
