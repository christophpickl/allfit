package allfit.presentation.view

import allfit.presentation.Styles
import allfit.presentation.htmlview
import allfit.presentation.logic.StaticImage
import allfit.presentation.logic.StaticImageStorage
import allfit.presentation.models.MainViewModel
import allfit.presentation.tornadofx.labelDetail
import allfit.presentation.tornadofx.labelPrompt
import allfit.presentation.tornadofx.openWebsiteButton
import allfit.presentation.tornadofx.setAllHeights
import tornadofx.View
import tornadofx.addClass
import tornadofx.bind
import tornadofx.hbox
import tornadofx.imageview
import tornadofx.label
import tornadofx.scrollpane
import tornadofx.vbox
import tornadofx.visibleWhen

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

                labelDetail("When", mainViewModel.selectedWorkout.map { it.date.prettyString })
                labelDetail("Where", mainViewModel.selectedWorkout.map { it.address })

                openWebsiteButton(mainViewModel.selectedWorkout.map { it.url })
            }
        }
        labelPrompt("About")
        htmlview(mainViewModel.selectedWorkout.map { it.about }) {
            setAllHeights(ViewConstants.detailTextHeight)
        }

        labelPrompt("Specifics")
        htmlview(mainViewModel.selectedWorkout.map { it.specifics }) {
            setAllHeights(ViewConstants.detailTextHeight)
        }
    }
}
