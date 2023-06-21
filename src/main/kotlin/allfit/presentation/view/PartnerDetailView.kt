package allfit.presentation.view

import allfit.presentation.PartnerModifications
import allfit.presentation.Styles
import allfit.presentation.UpdatePartnerFXEvent
import allfit.presentation.models.CurrentPartnerViewModel
import allfit.presentation.renderStars
import allfit.presentation.tornadofx.labelDetail
import allfit.presentation.tornadofx.labelPrompt
import allfit.presentation.tornadofx.openWebsiteButton
import allfit.presentation.tornadofx.setAllHeights
import allfit.presentation.tornadofx.setAllWidths
import allfit.service.Clock
import javafx.beans.value.ObservableValue
import javafx.scene.control.CheckBox
import javafx.scene.control.ComboBox
import javafx.scene.control.TextArea
import javafx.scene.layout.Border
import javafx.scene.layout.Priority
import tornadofx.FXEvent
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
import tornadofx.imageview
import tornadofx.label
import tornadofx.scrollpane
import tornadofx.selectedItem
import tornadofx.singleAssign
import tornadofx.textarea
import tornadofx.vbox

interface PartnerDetailModel {
    val selectedPartner: CurrentPartnerViewModel
}

class PartnerDetailView(model: PartnerDetailModel) : View() {

    private val clock: Clock by di()

    private var favoriteCheckbox: CheckBox by singleAssign()
    private var wishlistCheckbox: CheckBox by singleAssign()
    private var ratingInput: ComboBox<Number> by singleAssign()
    private var noteText: TextArea by singleAssign()

    private val enabledChecker: () -> ObservableValue<Boolean> = {
        model.selectedPartner.id.greaterThan(0)
    }

    override val root = vbox {

        hbox {
            scrollpane(fitToHeight = true) {
                setAllHeights(ViewConstants.bigImageHeight)
                setAllWidths(ViewConstants.bigImageHeight)
                imageview(model.selectedPartner.image)
            }

            vbox {
//                background = Background.fill(Color.RED)
                hgrow = Priority.ALWAYS
                label {
                    bind(model.selectedPartner.name)
                    addClass(Styles.header1)
                }

                labelDetail("Categories", model.selectedPartner.categoriesRendered, textMaxWidth = 300.0)
                labelDetail("Facilities", model.selectedPartner.facilitiesRendered, textMaxWidth = 300.0)
                labelDetail(
                    "Available",
                    model.selectedPartner.availability.map { it.toString() },
                    textMaxWidth = 100.0
                )

                labelPrompt("Description")
                textarea {
                    isEditable = false
                    border = Border.EMPTY
                    isWrapText = true
                    bind(model.selectedPartner.description)
                    setAllHeights(50.0)
                }

                openWebsiteButton(model.selectedPartner.url)
            }
        }

        hbox {
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
            setAllHeights(50.0)
        }

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

        hbox {
            vbox {
                labelPrompt("Upcoming Workouts")
                workoutTable(model.selectedPartner.upcomingWorkouts, ::fireDelegate, clock) {
                    setAllHeights(140.0)
                }
            }
            vbox {
                labelPrompt("Past Checkins")
                checkinTable(model.selectedPartner.pastCheckins, clock) {
                    setAllHeights(140.0)
                }
            }
        }
    }

    private fun fireDelegate(event: FXEvent) {
        fire(event)
    }
}
