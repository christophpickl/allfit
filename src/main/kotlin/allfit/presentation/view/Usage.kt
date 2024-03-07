package allfit.presentation.view

import allfit.presentation.Styles
import allfit.presentation.components.DualProgressIndicator
import allfit.presentation.components.ProgressIndicator
import allfit.presentation.models.Usage
import allfit.presentation.models.UsageModel
import allfit.presentation.tornadofx.labelDetail
import allfit.presentation.tornadofx.labelDetailMultibind
import allfit.service.formatDate
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import javafx.scene.paint.Color
import javafx.stage.Stage
import tornadofx.App
import tornadofx.View
import tornadofx.launch
import tornadofx.vbox

class UsageViewApp : App(
    primaryView = UsageView::class,
    stylesheet = Styles::class,
) {
    private val usageModel: UsageModel by inject()
    override fun start(stage: Stage) {
        super.start(stage)
        val now = ZonedDateTime.now()
        usageModel.today.set(now)
        usageModel.usage.set(
            Usage(
                totalCheckins = 13,
                noShows = 0,
                from = now.minusDays(5),
                until = now.plusDays(20),
                maxCheckinsInPeriod = 16,
                maxCheckInsOrReservationsPerPeriod = 4,
                totalCheckInsOrReservationsPerDay = 2,
                maxReservations = 6,
            )
        )
    }
}

class UsageView : View() {

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            launch<UsageViewApp>()
        }

        private val colorCheckins = Color.GREEN
        private val colorReservations = Color.BLUE
        private val colorPeriod = Color.CHOCOLATE
    }

    private val model: UsageModel by inject()

    override val root = vbox {
        labelDetailMultibind(
            prompt = "Usage",
            model.usage.map { it.totalCheckins }, model.usage.map { it.maxCheckinsInPeriod },
            textColor = colorCheckins
        ) {
            "${(model.usage.map { it.totalCheckins }.value)} / ${model.usage.map { it.maxCheckinsInPeriod }.value}"
        }
        labelDetailMultibind(
            "Reservations",
            model.usage.map { it.maxReservations }, model.reservationsInTotal,
            textColor = colorReservations
        ) {
            "${model.reservationsInTotal.get()} / ${model.usage.get().maxReservations}"
        }
        labelDetail(
            "Period",
            model.usage.map { "${it.from.formatDate()} t/m ${it.until.formatDate()}" },
            textColor = colorPeriod
        )

        val usageIndicator = DualProgressIndicator(
            colorCheckins, colorReservations,
            model.usage.map { (it.totalCheckins - model.reservationsInPeriod.value).toDouble() / it.maxCheckinsInPeriod },
            model.reservationsInPeriod.map { it.toDouble() / model.usage.map { it.maxCheckinsInPeriod }.value }
        )
        val periodIndicator = ProgressIndicator(
            colorPeriod, model.usage.map { it.daysUsed(model.today.get()).toDouble() / it.daysTotal() }
        )
        widthProperty().addListener { _, _, newValue ->
            usageIndicator.setMaxLineWidth(newValue.toDouble())
            periodIndicator.setMaxLineWidth(newValue.toDouble())
        }
        add(usageIndicator)
        add(periodIndicator)

        labelDetail("No shows", model.usage.map { it.noShows.toString() })

        labelDetail(
            "Max per day", model.usage.map { it.totalCheckInsOrReservationsPerDay.toString() }, smallSize = true
        )
        labelDetail(
            "Max per partner",
            model.usage.map { it.maxCheckInsOrReservationsPerPeriod.toString() },
            smallSize = true
        )
    }
}

private fun Usage.daysUsed(today: ZonedDateTime): Int =
    ChronoUnit.DAYS.between(from, today).toInt()

private fun Usage.daysTotal(): Int =
    ChronoUnit.DAYS.between(from, until).toInt()
