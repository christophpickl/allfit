package allfit.sync

import allfit.api.InMemoryOnefitClient
import allfit.api.models.ReservationJson
import allfit.api.models.reservationJson
import allfit.api.models.reservationsJsonRoot
import allfit.persistence.domain.InMemoryReservationsRepo
import allfit.persistence.domain.ReservationEntity
import allfit.persistence.reservationEntity
import allfit.service.toUtcLocalDateTime
import io.kotest.core.spec.style.StringSpec
import io.kotest.core.test.TestCase
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldBeSingleton
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import java.util.UUID

class ReservationsSyncerTest : StringSpec() {

    private val reservationEntity = Arb.reservationEntity().next()
    private val reservationJson = Arb.reservationJson().next()

    private lateinit var syncer: ReservationsSyncer
    private lateinit var client: InMemoryOnefitClient
    private lateinit var reservationsRepo: InMemoryReservationsRepo

    override suspend fun beforeEach(testCase: TestCase) {
        client = InMemoryOnefitClient()
        reservationsRepo = InMemoryReservationsRepo()
        syncer = ReservationsSyncerImpl(client, reservationsRepo)
    }

    init {
        "Given reservation Then insert it" {
            client.mockReservationsResponse(reservationJson)

            syncer.sync()

            reservationsRepo.selectAll().shouldBeSingleton()
                .first() shouldBe ReservationEntity(
                uuid = UUID.fromString(reservationJson.uuid),
                workoutId = reservationJson.workout.id,
                workoutStart = reservationJson.workout.from.toUtcLocalDateTime(),
            )
        }
        "Given reservation already exists Then ignore it" {
            reservationsRepo.insertAll(listOf(reservationEntity))
            client.mockReservationsResponse(reservationJson.copy(uuid = reservationEntity.uuid.toString()))

            syncer.sync()

            reservationsRepo.selectAll().shouldBeSingleton()
                .first().uuid shouldBe reservationEntity.uuid
        }
        "Given reservation not existing locally Then delete it" {
            reservationsRepo.insertAll(listOf(reservationEntity))

            syncer.sync()

            reservationsRepo.selectAll().shouldBeEmpty()
        }
    }
}

private fun InMemoryOnefitClient.mockReservationsResponse(vararg reservations: ReservationJson) {
    reservationsJson = Arb.reservationsJsonRoot().next().copy(data = reservations.toList())
}
