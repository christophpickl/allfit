package allfit.persistence

import allfit.persistence.domain.ExposedReservationsRepo
import allfit.persistence.testInfra.DbListener
import allfit.persistence.testInfra.ExposedTestRepo
import allfit.persistence.testInfra.reservationEntity
import allfit.uuid1
import allfit.uuid2
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldBeSingleton
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import org.jetbrains.exposed.exceptions.ExposedSQLException
import java.time.LocalDateTime

class ExposedReservationsRepoTest : DescribeSpec() {

    private val repo = ExposedReservationsRepo
    private val reservation = Arb.reservationEntity().next()
    private val reservation1 = Arb.reservationEntity().next().copy(uuid = uuid1)
    private val reservation2 = Arb.reservationEntity().next().copy(uuid = uuid2)
    private val now = LocalDateTime.now()

    init {
        extension(DbListener())

        describe("When Insert") {
            it("Given requirements Then selected") {
                val reservation = ExposedTestRepo.insertCategoryPartnerAndWorkoutForReservation()
                repo.insertAll(listOf(reservation))

                repo.selectAllStartingFrom(reservation.workoutStart).shouldBeSingleton().first() shouldBe reservation
            }
            it("Given no requirements Then fail") {
                shouldThrow<ExposedSQLException> {
                    repo.insertAll(listOf(reservation))
                }
            }
        }
        describe("When select") {
            it("Given requirements When select before start Then return nothing") {
                val reservation = ExposedTestRepo.insertCategoryPartnerAndWorkoutForReservation()
                repo.insertAll(listOf(reservation))

                repo.selectAllStartingFrom(reservation.workoutStart.plusSeconds(1L)).shouldBeEmpty()
            }
        }
        describe("When delete") {
            it("Given workout and two reservations When delete one Then only keep other") {
                val preparedReservation = ExposedTestRepo.insertCategoryPartnerAndWorkoutForReservation()
                repo.insertAll(listOf(reservation1, reservation2).map {
                    it.copy(
                        workoutId = preparedReservation.workoutId,
                        workoutStart = now
                    )
                })

                repo.deleteAll(listOf(reservation1.uuid))

                repo.selectAllStartingFrom(now).shouldBeSingleton().first().uuid shouldBe reservation2.uuid
            }
        }
    }

}
