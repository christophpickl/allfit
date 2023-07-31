package allfit.presentation.workouts

import allfit.presentation.Styles
import allfit.presentation.components.bigImage
import allfit.presentation.htmlview
import allfit.presentation.logic.StaticIcon
import allfit.presentation.logic.StaticIconStorage
import allfit.presentation.logic.googleMapsSearchUrl
import allfit.presentation.models.FullWorkout
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
import tornadofx.tooltip
import tornadofx.vbox
import tornadofx.visibleWhen

interface WorkoutDetailModel {
    val selectedWorkout: SimpleObjectProperty<FullWorkout>
}

class WorkoutDetailView(
    private val workoutModel: WorkoutDetailModel
) : View() {

    private val clock: Clock by di()

    override val root = vbox {
//        background = Background.fill(Color.BLUE)
        hbox {
            bigImage(workoutModel.selectedWorkout.map { it.image })

            vbox {
                hbox {
                    label(workoutModel.selectedWorkout.map { it.name }) {
                        addClass(Styles.header1)
                        maxWidth = 550.0
                        tooltip {
                            workoutModel.selectedWorkout.map { it.name }.addListener { _, _, newName ->
                                text = newName
                            }
                        }
                    }
                    imageview(StaticIconStorage.get(StaticIcon.Reserved)) {
                        visibleWhen {
                            workoutModel.selectedWorkout.map { it.isReserved }
                        }
                    }
                }

                labelDetail("When", workoutModel.selectedWorkout.map { it.date.toPrettyString(clock) })
                val addressBind = workoutModel.selectedWorkout.map { it.address }
                labelDetail("Where", addressBind, link = googleMapsSearchUrl(addressBind))
                labelDetail("Teacher", workoutModel.selectedWorkout.map { it.teacher })
                openWebsiteButton(workoutModel.selectedWorkout.map { it.url })
            }
        }
        labelPrompt("About")
        htmlview(workoutModel.selectedWorkout.map { it.about }) {
            setAllHeights(ViewConstants.detailTextHeight)
        }

        labelPrompt("Specifics")
        htmlview(workoutModel.selectedWorkout.map { it.specifics }) {
            setAllHeights(ViewConstants.detailTextHeight)
        }
    }
}
