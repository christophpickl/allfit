package allfit.presentation.view

import allfit.presentation.Styles
import allfit.presentation.htmlview
import allfit.presentation.logic.StaticImage
import allfit.presentation.logic.StaticImageStorage
import allfit.presentation.models.MainViewModel
import allfit.presentation.tornadofx.setAllHeights
import tornadofx.*
import java.awt.Desktop
import java.net.URI

class WorkoutDetailView : View() {

    private val mainViewModel: MainViewModel by inject()

    override val root = vbox {
        hbox {
            scrollpane(fitToHeight = true) {
                setAllHeights(ViewConstants.bigImageHeight)
                imageview(mainViewModel.selectedWorkout.map {
                    it.image
                })
            }

            vbox {
                hbox {
                    label("") {
                        bind(mainViewModel.selectedWorkout.map {
                            it.name
                        })
                        addClass(Styles.header1)
                    }
                    imageview(StaticImageStorage.get(StaticImage.Reserved)) {
                        visibleWhen {
                            mainViewModel.selectedWorkout.map { it.isReserved }
                        }
                    }
                }

                label().bind(mainViewModel.selectedWorkout.map {
                    it.date.prettyString
                })
                label().bind(mainViewModel.selectedWorkout.map {
                    "Address: ${it.address}"
                })

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

        htmlview(mainViewModel.selectedWorkout.map { it.about }) {
            setAllHeights(ViewConstants.detailTextHeight)
        }
        htmlview(mainViewModel.selectedWorkout.map { it.specifics }) {
            setAllHeights(ViewConstants.detailTextHeight)
        }
    }
}
