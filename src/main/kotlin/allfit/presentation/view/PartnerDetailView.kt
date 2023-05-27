package allfit.presentation.view

import allfit.presentation.*
import allfit.presentation.models.MainViewModel
import allfit.presentation.models.SimpleWorkout
import allfit.presentation.tornadofx.labelDetail
import allfit.presentation.tornadofx.labelPrompt
import allfit.presentation.tornadofx.openWebsiteButton
import allfit.presentation.tornadofx.setAllHeights
import javafx.beans.value.ObservableValue
import javafx.collections.ObservableList
import javafx.event.EventTarget
import javafx.scene.control.*
import javafx.scene.layout.Border
import tornadofx.*
import kotlin.error

class PartnerDetailView : View() {

    private val mainViewModel: MainViewModel by inject()
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
                imageview(mainViewModel.selectedPartner.image)
            }

            vbox {
                label("Partner") {
                    bind(mainViewModel.selectedPartner.name)
                    addClass(Styles.header1)
                }

                labelDetail("Categories", mainViewModel.selectedPartner.categoriesRendered)
                labelDetail("Facilities", mainViewModel.selectedPartner.facilitiesRendered)

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
                workoutTable(mainViewModel.selectedPartner.upcomingWorkouts, ::fireDelegate) {
                    setAllHeights(140.0)
                }
            }
            vbox {
                labelPrompt("Visited Workouts")
                workoutTable(mainViewModel.selectedPartner.visitedWorkouts, ::fireDelegate) {
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
    withTable: WorkoutTable.() -> Unit,
) = WorkoutTable(items, onSelected).attachTo(this, withTable)

class WorkoutTable(items: ObservableList<SimpleWorkout>, onSelected: (PartnerWorkoutSelectedFXEvent) -> Unit) :
    TableView<SimpleWorkout>(items) {
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
                text = date.prettyString
            }
        }

        smartResize()
    }
}