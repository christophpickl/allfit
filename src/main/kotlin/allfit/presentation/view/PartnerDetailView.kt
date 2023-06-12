package allfit.presentation.view

import allfit.presentation.PartnerModifications
import allfit.presentation.PartnerWorkoutSelectedFXEvent
import allfit.presentation.Styles
import allfit.presentation.UpdatePartnerFXEvent
import allfit.presentation.models.MainViewModel
import allfit.presentation.models.SimpleWorkout
import allfit.presentation.renderStars
import allfit.presentation.tornadofx.labelDetail
import allfit.presentation.tornadofx.labelPrompt
import allfit.presentation.tornadofx.openWebsiteButton
import allfit.presentation.tornadofx.setAllHeights
import allfit.presentation.tornadofx.setAllWidths
import allfit.service.Clock
import javafx.beans.value.ObservableValue
import javafx.collections.ObservableList
import javafx.event.EventTarget
import javafx.scene.control.CheckBox
import javafx.scene.control.ComboBox
import javafx.scene.control.SelectionMode
import javafx.scene.control.TableView
import javafx.scene.control.TextArea
import javafx.scene.layout.Border
import javafx.scene.layout.Priority
import tornadofx.FXEvent
import tornadofx.View
import tornadofx.action
import tornadofx.addClass
import tornadofx.attachTo
import tornadofx.bind
import tornadofx.button
import tornadofx.cellFormat
import tornadofx.checkbox
import tornadofx.column
import tornadofx.combobox
import tornadofx.enableWhen
import tornadofx.fixedWidth
import tornadofx.hbox
import tornadofx.hgrow
import tornadofx.imageview
import tornadofx.label
import tornadofx.onDoubleClick
import tornadofx.onSelectionChange
import tornadofx.readonlyColumn
import tornadofx.remainingWidth
import tornadofx.scrollpane
import tornadofx.selectedItem
import tornadofx.singleAssign
import tornadofx.smartResize
import tornadofx.textarea
import tornadofx.vbox

class PartnerDetailView : View() {

    private val mainViewModel: MainViewModel by inject()
    private val clock: Clock by di()

    private var favoriteCheckbox: CheckBox by singleAssign()
    private var wishlistCheckbox: CheckBox by singleAssign()
    private var ratingInput: ComboBox<Number> by singleAssign()
    private var noteText: TextArea by singleAssign()

    private val enabledChecker: () -> ObservableValue<Boolean> = {
        mainViewModel.selectedPartner.id.greaterThan(0)
    }

    override val root = vbox {

        hbox {
            scrollpane(fitToHeight = true) {
                setAllHeights(ViewConstants.bigImageHeight)
                setAllWidths(ViewConstants.bigImageHeight)
                imageview(mainViewModel.selectedPartner.image)
            }

            vbox {
//                background = Background.fill(Color.RED)
                hgrow = Priority.ALWAYS
                label {
                    bind(mainViewModel.selectedPartner.name)
                    addClass(Styles.header1)
                }

                labelDetail("Categories", mainViewModel.selectedPartner.categoriesRendered, textMaxWidth = 300.0)
                labelDetail("Facilities", mainViewModel.selectedPartner.facilitiesRendered, textMaxWidth = 300.0)

                labelPrompt("Description")
                textarea {
                    isEditable = false
                    border = Border.EMPTY
                    isWrapText = true
                    bind(mainViewModel.selectedPartner.description)
                    setAllHeights(50.0)
                }

                openWebsiteButton(mainViewModel.selectedPartner.url)
            }
        }

        hbox {
            labelPrompt("Rating")
            ratingInput = combobox(values = listOf<Number>(0, 1, 2, 3, 4, 5)) {
                bind(mainViewModel.selectedPartner.rating)
                cellFormat {
                    text = it.toInt().renderStars()
                }
                enableWhen(enabledChecker)
            }

            labelPrompt("Favorite")
            favoriteCheckbox = checkbox {
                bind(mainViewModel.selectedPartner.isFavorited)
                enableWhen(enabledChecker)
            }

            labelPrompt("Wishlist")
            wishlistCheckbox = checkbox {
                bind(mainViewModel.selectedPartner.isWishlisted)
                enableWhen(enabledChecker)
            }
        }

        labelPrompt("Note")
        noteText = textarea {
            bind(mainViewModel.selectedPartner.note)
            enableWhen(enabledChecker)
            setAllHeights(50.0)
        }

        button("Update Partner") {
            enableWhen(enabledChecker)
            action {
                fire(
                    UpdatePartnerFXEvent(
                        PartnerModifications(
                            partnerId = mainViewModel.selectedPartner.id.get(),
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
                workoutTable(mainViewModel.selectedPartner.upcomingWorkouts, ::fireDelegate, clock) {
                    setAllHeights(140.0)
                }
            }
            vbox {
                labelPrompt("Visited Workouts")
                workoutTable(mainViewModel.selectedPartner.visitedWorkouts, ::fireDelegate, clock) {
                    setAllHeights(140.0)
                }
            }
        }
    }

    private fun fireDelegate(event: FXEvent) {
        fire(event)
    }
}

fun EventTarget.workoutTable(
    items: ObservableList<SimpleWorkout>,
    onSelected: (PartnerWorkoutSelectedFXEvent) -> Unit,
    clock: Clock,
    withTable: WorkoutTable.() -> Unit,
) = WorkoutTable(items, onSelected, clock).attachTo(this, withTable)

class WorkoutTable(
    items: ObservableList<SimpleWorkout>,
    onSelected: (PartnerWorkoutSelectedFXEvent) -> Unit,
    clock: Clock,
) : TableView<SimpleWorkout>(items) {
    init {
        selectionModel.selectionMode = SelectionMode.SINGLE

        onSelectionChange {
            selectedItem?.let {
                onSelected(PartnerWorkoutSelectedFXEvent(it))
            }
        }
        onDoubleClick {
            selectedItem?.let {
                onSelected(PartnerWorkoutSelectedFXEvent(it))
            }
        }

        column<SimpleWorkout, String>("Name") {
            it.value.nameProperty()
        }.apply {
            minWidth = 250.0
            remainingWidth()
        }

        readonlyColumn("Date", SimpleWorkout::date).apply {
            fixedWidth(150)
            cellFormat { date ->
                text = date.toPrettyString(clock)
            }
        }

        smartResize()
    }
}