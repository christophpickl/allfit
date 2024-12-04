@file:UseSerializers(ZonedDateTimeSerializer::class)

package allfit.service

import allfit.api.models.ZonedDateTimeSerializer
import allfit.persistence.domain.CheckinType
import allfit.persistence.domain.CheckinsRepository
import allfit.persistence.domain.PartnerEntity
import allfit.persistence.domain.PartnersRepo
import allfit.persistence.domain.WorkoutsRepo
import allfit.presentation.preferences.ExportFXEvent
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.transactions.transaction
import tornadofx.Controller
import java.time.ZonedDateTime

@Serializable
data class Export(
    val partners: List<EPartner>
)

@Serializable
data class EPartner(
    val id: Int,
    val name: String,
    val rating: Int,
    val note: String,
    val website: String?,
    val isWishlisted: Boolean,
    val isFavorited: Boolean,
    val dropins: MutableList<EDropin> = mutableListOf(),
    val checkins: MutableList<ECheckin> = mutableListOf(),
)

@Serializable
data class EDropin(
    val createdAt: ZonedDateTime
)

@Serializable
data class ECheckin(
    val workoutName: String,
    val start: ZonedDateTime,
    val end: ZonedDateTime,
)

class Exporter : Controller() {

    private val partnersRepo: PartnersRepo by di()
    private val workoutsRepo: WorkoutsRepo by di()
    private val checkinsRepo: CheckinsRepository by di()

    init {
        subscribe<ExportFXEvent> {
            transaction {
                val partners = partnersRepo.selectAll()
                val workouts = workoutsRepo.selectAll()
                val checkins = checkinsRepo.selectAll()
                println("exporting ${partners.size} partners and ${workouts.size} workouts and ${checkins.size} checkins")

                val epartners = mutableListOf<EPartner>()
                checkins.forEach { check ->
                    val partner = partners.single { it.id == check.partnerId }
                    var epartner = epartners.find { it.id == partner.id }
                    if (epartner == null) {
                        epartner = partner.toEPartner()
                        epartners += epartner
                    }

                    if (check.type == CheckinType.DROP_IN) {
                        epartner.dropins += EDropin(check.createdAt.fromUtcToAmsterdamZonedDateTime())
                    } else {
                        val workout = workouts.single { it.id == check.workoutId }
                        epartner.checkins += ECheckin(
                            workoutName = workout.name,
                            start = workout.start.fromUtcToAmsterdamZonedDateTime(),
                            end = workout.end.fromUtcToAmsterdamZonedDateTime(),
                        )
                    }
                }

                val root = Export(epartners)
                val string = Json.encodeToString(root)
                println()
                println()
                println(string)
            }
        }
    }
}

private fun PartnerEntity.toEPartner() = EPartner(
    id = id,
    name = name,
    rating = rating,
    note = note,
    website = officialWebsite,
    isWishlisted = isWishlisted,
    isFavorited = isFavorited,
)
