package allfit.presentation.partners

import allfit.presentation.PartnerWorkoutSelectedFXEvent
import allfit.presentation.Styles
import allfit.presentation.UpdatePartnerFXEvent
import allfit.presentation.WorkoutSelectedThrough
import allfit.presentation.components.bigImage
import allfit.presentation.components.favoriteToggleButton
import allfit.presentation.components.wishlistToggleButton
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
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.value.ObservableValue
import javafx.geometry.Pos
import javafx.scene.control.ComboBox
import javafx.scene.control.TextArea
import javafx.scene.control.ToggleButton
import javafx.scene.layout.Background
import javafx.scene.layout.Priority
import kotlin.reflect.KFunction1
import kotlin.reflect.KProperty1
import tornadofx.View
import tornadofx.action
import tornadofx.addClass
import tornadofx.bind
import tornadofx.button
import tornadofx.combobox
import tornadofx.enableWhen
import tornadofx.fitToParentHeight
import tornadofx.hbox
import tornadofx.hgrow
import tornadofx.label
import tornadofx.paddingBottom
import tornadofx.paddingTop
import tornadofx.selectedItem
import tornadofx.singleAssign
import tornadofx.textarea
import tornadofx.textfield
import tornadofx.tooltip
import tornadofx.vbox
import tornadofx.vgrow

class PartnerDetailView(
    selectedPartner: CurrentPartnerViewModel,
    private val selectedThrough: WorkoutSelectedThrough,
) : View() {

    private val clock: Clock by di()

    private var favoriteButton: ToggleButton by singleAssign()
    private var wishlistButton: ToggleButton by singleAssign()
    private var ratingInput: ComboBox<Number> by singleAssign()
    private var noteText: TextArea by singleAssign()
    private val officialWebsiteText = textfield(selectedPartner.officialWebsite) {
        hgrow = Priority.ALWAYS
    }

    private val enabledChecker: () -> ObservableValue<Boolean> = {
        selectedPartner.id.greaterThan(0)
    }

    override val root = vbox {
        hbox(spacing = 5.0) {
            bigImage(selectedPartner.image, background = Background.fill(Styles.colorImageBigBg))

            vbox {
                label(selectedPartner.name) {
                    addClass(Styles.header1)
                    maxWidth = 550.0
                    tooltip {
                        selectedPartner.name.addListener { _, _, newName ->
                            text = newName
                        }
                    }
                }

                labelDetail("Categories", selectedPartner.categoriesRendered, textMaxWidth = 450.0)
                labelDetail("Facilities", selectedPartner.facilitiesRendered, textMaxWidth = 450.0)
                labelDetail(
                    "Available",
                    selectedPartner.availability.map { it.toString() },
                    textMaxWidth = 100.0
                )

                labelPrompt("Description")
                htmlview(selectedPartner.description) {
                    setAllHeights(65.0)
                }
                hbox {
                    paddingTop = 5.0
                    openWebsiteButton(selectedPartner.url, "Partner Website")
                }
            }
            paddingBottom = 5.0
        }

        hbox(spacing = 5.0, alignment = Pos.CENTER_LEFT) {
            labelPrompt("Rating")
            ratingInput = combobox(values = listOf<Number>(0, 1, 2, 3, 4, 5)) {
                bind(selectedPartner.rating)
                cellFormat {
                    text = it.toInt().renderStars()
                }
                enableWhen(enabledChecker)
            }

            val makeToggleButton = { label: String,
                                     toggleFun: KFunction1<ToggleButton.() -> Unit, ToggleButton>,
                                     booleanProp: KProperty1<CurrentPartnerViewModel, SimpleBooleanProperty> ->
                labelPrompt(label)
                toggleFun {
                    selectedPartner.id.addListener { _, _, _ ->
                        isSelected = booleanProp.get(selectedPartner).get()
                    }
                    enableWhen(enabledChecker)
                    fitToParentHeight()
                }
            }

            favoriteButton = makeToggleButton("Favorite", ::favoriteToggleButton, CurrentPartnerViewModel::isFavorited)
            wishlistButton = makeToggleButton("Wishlist", ::wishlistToggleButton, CurrentPartnerViewModel::isWishlisted)
        }

        hbox(spacing = 5.0, alignment = Pos.CENTER_LEFT) {
            paddingTop = 5.0
            openWebsiteButton(officialWebsiteText.textProperty(), "Partner Official Website") {
                hgrow = Priority.NEVER
            }
            add(officialWebsiteText)
        }

        labelPrompt("Note")
        noteText = textarea {
            bind(selectedPartner.note)
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
                                partnerId = selectedPartner.id.get(),
                                note = noteText.text,
                                rating = ratingInput.selectedItem?.toInt() ?: error("No rating selected!"),
                                officialWebsite = officialWebsiteText.text.let { it.ifEmpty { null } },
                                isFavorited = favoriteButton.isSelected,
                                isWishlisted = wishlistButton.isSelected,
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
                workoutTable(selectedPartner.upcomingWorkouts, ::workoutSelectedDelegate, clock)
            }
            vbox {
                labelPrompt("Past Checkins")
                checkinTable(selectedPartner.pastCheckins, ::workoutSelectedDelegate, clock)
            }
        }
    }

    private fun workoutSelectedDelegate(workout: SimpleWorkout) {
        fire(PartnerWorkoutSelectedFXEvent(workout, selectedThrough))
    }
}
