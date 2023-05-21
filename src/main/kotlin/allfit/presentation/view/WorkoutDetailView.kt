package allfit.presentation.view

import allfit.presentation.Styles
import allfit.presentation.htmlview
import allfit.presentation.logic.StaticImage
import allfit.presentation.logic.StaticImageStorage
import allfit.presentation.models.MainViewModel
import tornadofx.*
import java.awt.Desktop
import java.net.URI

class WorkoutDetailView : View() {

    private val mainViewModel: MainViewModel by inject()

    override val root = vbox {
        hbox {
            label("") {
                bind(mainViewModel.selectedWorkout.map { it.name })
                addClass(Styles.header1)
            }
            imageview(StaticImageStorage.get(StaticImage.Reserved)) {
                visibleWhen {
                    mainViewModel.selectedWorkout.map { it.isReserved }
                }
            }
        }
        hbox {
            imageview(mainViewModel.selectedWorkout.map { it.image })
            vbox {
                label().bind(mainViewModel.selectedWorkout.map { it.date.prettyString })
                label().bind(mainViewModel.selectedWorkout.map {
                    "Address: ${it.address}"
                })
            }
        }

        scrollpane {
            htmlview(mainViewModel.selectedWorkout.map { it.about }) {
            }
        }
        htmlview(mainViewModel.selectedWorkout.map { it.specifics }) {
//            prefHeight = 100.0
        }

        button("Open Website") {
            tooltip {
                this@tooltip.textProperty().bind(mainViewModel.selectedWorkout.map { it.url })
            }
            action {
                Desktop.getDesktop().browse(URI(mainViewModel.selectedWorkout.value.url))
            }
        }
    }
}
