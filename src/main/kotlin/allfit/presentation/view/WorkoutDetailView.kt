package allfit.presentation.view

import allfit.presentation.Styles
import allfit.presentation.components.bigImage
import allfit.presentation.htmlview
import allfit.presentation.logic.StaticIcon
import allfit.presentation.logic.StaticIconStorage
import allfit.presentation.logic.googleMapsSearchUrl
import allfit.presentation.models.MainViewModel
import allfit.presentation.tornadofx.labelDetail
import allfit.presentation.tornadofx.labelPrompt
import allfit.presentation.tornadofx.openWebsiteButton
import allfit.presentation.tornadofx.setAllHeights
import allfit.service.Clock
import tornadofx.View
import tornadofx.addClass
import tornadofx.hbox
import tornadofx.imageview
import tornadofx.label
import tornadofx.tooltip
import tornadofx.vbox
import tornadofx.visibleWhen

class WorkoutDetailView : View() {

    private val mainViewModel: MainViewModel by inject()
    private val clock: Clock by di()

    override val root = vbox {
//        background = Background.fill(Color.BLUE)
        hbox {
            bigImage(mainViewModel.selectedWorkout.map { it.image })

            vbox {
                hbox {
                    label(mainViewModel.selectedWorkout.map { it.name }) {
                        addClass(Styles.header1)
                        maxWidth = 550.0
                        tooltip {
                            mainViewModel.selectedWorkout.map { it.name }.addListener { _, _, newName ->
                                text = newName
                            }
                        }
                    }
                    imageview(StaticIconStorage.get(StaticIcon.Reserved)) {
                        visibleWhen {
                            mainViewModel.selectedWorkout.map { it.isReserved }
                        }
                    }
                }

                labelDetail("When", mainViewModel.selectedWorkout.map { it.date.toPrettyString(clock) })
                val addressBind = mainViewModel.selectedWorkout.map { it.address }
                labelDetail("Where", addressBind, link = googleMapsSearchUrl(addressBind))

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
