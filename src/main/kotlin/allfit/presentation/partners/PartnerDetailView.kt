package allfit.presentation.partners

import allfit.presentation.PartnerModifications
import allfit.presentation.PartnerWorkoutSelectedFXEvent
import allfit.presentation.Styles
import allfit.presentation.UpdatePartnerFXEvent
import allfit.presentation.WorkoutSelectedThrough
import allfit.presentation.components.bigImage
import allfit.presentation.htmlview
import allfit.presentation.models.SimpleWorkout
import allfit.presentation.renderStars
import allfit.presentation.tornadofx.labelDetail
import allfit.presentation.tornadofx.labelPrompt
import allfit.presentation.tornadofx.openWebsiteButton
import allfit.presentation.tornadofx.setAllHeights
import allfit.presentation.view.checkinTable
import allfit.presentation.workouts.CurrentPartnerViewModel
import allfit.presentation.workouts.workoutTable
import allfit.service.Clock
import javafx.beans.value.ObservableValue
import javafx.geometry.Pos
import javafx.scene.control.CheckBox
import javafx.scene.control.ComboBox
import javafx.scene.control.TextArea
import javafx.scene.layout.Priority
import tornadofx.View
import tornadofx.action
import tornadofx.addClass
import tornadofx.bind
import tornadofx.button
import tornadofx.checkbox
import tornadofx.combobox
import tornadofx.enableWhen
import tornadofx.hbox
import tornadofx.hgrow
import tornadofx.label
import tornadofx.paddingBottom
import tornadofx.paddingTop
import tornadofx.selectedItem
import tornadofx.singleAssign
import tornadofx.textarea
import tornadofx.tooltip
import tornadofx.vbox
import tornadofx.vgrow

interface PartnerDetailModel {
    val selectedPartner: CurrentPartnerViewModel
}

class PartnerDetailView(
    model: PartnerDetailModel,
    private val selectedThrough: WorkoutSelectedThrough,
) : View() {

    private val clock: Clock by di()

    private var favoriteCheckbox: CheckBox by singleAssign()
    private var wishlistCheckbox: CheckBox by singleAssign()
    private var ratingInput: ComboBox<Number> by singleAssign()
    private var noteText: TextArea by singleAssign()

    private val enabledChecker: () -> ObservableValue<Boolean> = {
        model.selectedPartner.id.greaterThan(0)
    }

    override val root = vbox {
        hbox(spacing = 5.0) {
            bigImage(model.selectedPartner.image)

            vbox {
                label(model.selectedPartner.name) {
                    addClass(Styles.header1)
                    maxWidth = 550.0
                    tooltip {
                        model.selectedPartner.name.addListener { _, _, newName ->
                            text = newName
                        }
                    }
                }

                labelDetail("Categories", model.selectedPartner.categoriesRendered, textMaxWidth = 450.0)
                labelDetail("Facilities", model.selectedPartner.facilitiesRendered, textMaxWidth = 450.0)
                labelDetail(
                    "Available",
                    model.selectedPartner.availability.map { it.toString() },
                    textMaxWidth = 100.0
                )

                labelPrompt("Description")
                htmlview(model.selectedPartner.description) {
                    setAllHeights(60.0)
                }
                openWebsiteButton(model.selectedPartner.url)
            }
            paddingBottom = 5.0
        }

        hbox(spacing = 5.0, alignment = Pos.CENTER_LEFT) {
            labelPrompt("Rating")
            ratingInput = combobox(values = listOf<Number>(0, 1, 2, 3, 4, 5)) {
                bind(model.selectedPartner.rating)
                cellFormat {
                    text = it.toInt().renderStars()
                }
                enableWhen(enabledChecker)
            }

            labelPrompt("Favorite")
            favoriteCheckbox = checkbox {
                bind(model.selectedPartner.isFavorited)
                enableWhen(enabledChecker)
            }

            labelPrompt("Wishlist")
            wishlistCheckbox = checkbox {
                bind(model.selectedPartner.isWishlisted)
                enableWhen(enabledChecker)
            }
        }

        labelPrompt("Note")
        noteText = textarea {
            bind(model.selectedPartner.note)
            enableWhen(enabledChecker)
            setAllHeights(70.0)
        }

        hbox {
            button("Update Partner") {
                enableWhen(enabledChecker)
                action {
                    fire(
                        UpdatePartnerFXEvent(
                            PartnerModifications(
                                partnerId = model.selectedPartner.id.get(),
                                note = noteText.text,
                                rating = ratingInput.selectedItem?.toInt() ?: error("No rating selected!"),
                                isFavorited = favoriteCheckbox.isSelected,
                                isWishlisted = wishlistCheckbox.isSelected,
                            )
                        )
                    )
                }
            }
            paddingTop = 5.0
            paddingBottom = 10.0
        }

        hbox(spacing = 5.0) {
            vgrow = Priority.ALWAYS
            hgrow = Priority.ALWAYS
            vbox {
                labelPrompt("Upcoming Workouts")
                workoutTable(model.selectedPartner.upcomingWorkouts, ::fireDelegate, clock)
            }
            vbox {
                labelPrompt("Past Checkins")
                checkinTable(model.selectedPartner.pastCheckins, clock)
            }
        }
    }

    private fun fireDelegate(workout: SimpleWorkout) {
        fire(PartnerWorkoutSelectedFXEvent(workout, selectedThrough))
    }
}
