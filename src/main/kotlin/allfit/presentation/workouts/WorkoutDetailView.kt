package allfit.presentation.workouts

import allfit.presentation.Styles
import allfit.presentation.htmlview
import allfit.presentation.logic.StaticIcon
import allfit.presentation.logic.StaticIconStorage
import allfit.presentation.logic.googleMapsSearchUrl
import allfit.presentation.models.FullWorkout
import allfit.presentation.tornadofx.copyToClipboard
import allfit.presentation.tornadofx.labelDetail
import allfit.presentation.tornadofx.labelPrompt
import allfit.presentation.tornadofx.openWebsiteButton
import allfit.presentation.tornadofx.setAllHeights
import allfit.presentation.view.ViewConstants
import allfit.service.Clock
import javafx.beans.property.SimpleObjectProperty
import tornadofx.View
import tornadofx.addClass
import tornadofx.hbox
import tornadofx.imageview
import tornadofx.label
import tornadofx.paddingBottom
import tornadofx.tooltip
import tornadofx.vbox
import tornadofx.visibleWhen

class WorkoutDetailView(
    private val selectedWorkout: SimpleObjectProperty<FullWorkout>
) : View() {

    private val clock: Clock by di()

    override val root = vbox {
//        background = Background.fill(Color.BLUE)
        hbox(spacing = 5.0) {

            vbox {
                hbox {
                    label(selectedWorkout.map { it.name }) {
                        addClass(Styles.header1)
                        maxWidth = 550.0
                        tooltip {
                            selectedWorkout.map { it.name }.addListener { _, _, newName ->
                                text = newName
                            }
                        }
                    }
                    imageview(StaticIconStorage.get(StaticIcon.Reserved)) {
                        visibleWhen {
                            selectedWorkout.map { it.isReserved }
                        }
                    }
                }

                labelDetail("When", selectedWorkout.map { it.date.toPrettyString(clock) })
                labelDetail(
                    prompt = "Where",
                    value = selectedWorkout.map { it.address },
                    link = selectedWorkout.map { googleMapsSearchUrl(it.address) },
                    isExternal = true,
                    contextMenu = mapOf("Copy to clipboard" to { copyToClipboard(selectedWorkout.get().address) })
                )
                labelDetail("Teacher", selectedWorkout.map { it.teacher })
                openWebsiteButton(selectedWorkout.map { it.url }, "Workout Website")
            }
            paddingBottom = 5.0
        }

        labelPrompt("About")
        htmlview(selectedWorkout.map { it.about }) {
            setAllHeights(ViewConstants.DETAIL_TEXT_HEIGHT)
        }

        labelPrompt("Specifics")
        htmlview(selectedWorkout.map { it.specifics }) {
            setAllHeights(ViewConstants.DETAIL_TEXT_HEIGHT)
        }
    }
}
