package allfit.presentation.view

import allfit.presentation.PartnerModifications
import allfit.presentation.PartnerWorkoutSelectedFXEvent
import allfit.presentation.Styles
import allfit.presentation.UpdatePartnerFXEvent
import allfit.presentation.models.MainViewModel
import allfit.presentation.models.SimpleWorkout
import allfit.presentation.renderStars
import javafx.beans.value.ObservableValue
import javafx.collections.ObservableList
import javafx.event.EventTarget
import javafx.geometry.Orientation
import javafx.scene.control.CheckBox
import javafx.scene.control.ComboBox
import javafx.scene.control.SelectionMode
import javafx.scene.control.TableView
import javafx.scene.control.TextArea
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
import tornadofx.field
import tornadofx.fieldset
import tornadofx.fixedWidth
import tornadofx.form
import tornadofx.hbox
import tornadofx.imageview
import tornadofx.label
import tornadofx.onDoubleClick
import tornadofx.onSelectionChange
import tornadofx.readonlyColumn
import tornadofx.selectedItem
import tornadofx.singleAssign
import tornadofx.textarea
import tornadofx.tooltip
import tornadofx.vbox
import java.awt.Desktop
import java.net.URI

// model dirty check/commit tutorial: https://docs.tornadofx.io/0_subsection/11_editing_models_and_validation
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
        label("Partner") {
            bind(mainViewModel.selectedPartner.name)
            addClass(Styles.header1)
        }
        imageview(mainViewModel.selectedPartner.image)
        form {
            fieldset(labelPosition = Orientation.HORIZONTAL) {

                field("Rating:") {
                    ratingInput = combobox(values = listOf<Number>(0, 1, 2, 3, 4, 5)) {
                        bind(mainViewModel.selectedPartner.rating)
                        cellFormat {
                            text = it.toInt().renderStars()
                        }
                    }
                }
                field("Favorite:") {
                    favoriteCheckbox = checkbox {
                        bind(mainViewModel.selectedPartner.isFavorited)
                        enableWhen(enabledChecker)
                    }
                }
                field("Wishlist:") {
                    wishlistCheckbox = checkbox {
                        bind(mainViewModel.selectedPartner.isWishlisted)
                        enableWhen(enabledChecker)
                    }
                }
                field("Note:") {
                    noteText = textarea {
                        bind(mainViewModel.selectedPartner.note)
                        enableWhen(enabledChecker)
                    }
                }
            }
        }
        hbox {
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
            button("Open Website") {
                tooltip {
                    this@tooltip.textProperty().bind(mainViewModel.selectedPartner.url)
                }
                action {
                    Desktop.getDesktop().browse(URI(mainViewModel.selectedPartner.url.value))
                }
            }
        }
        label("Facilities:")
        label {
            bind(mainViewModel.selectedPartner.facilities.map { it.split(",").joinToString() })
        }

        textarea("Description:") {
            isEditable = false
            // TODO render as HTML
            bind(mainViewModel.selectedPartner.description)
        }

        label("Visited Workouts:")
        workoutTable(mainViewModel.selectedPartner.pastWorkouts, ::fireDelegate) {
            prefHeight = 60.0
        }

        label("Available Workouts:")
        workoutTable(mainViewModel.selectedPartner.currentWorkouts, ::fireDelegate) {
            prefHeight = 120.0
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

        column<SimpleWorkout, String>("Name") { it.value.nameProperty() }
        readonlyColumn("Date", SimpleWorkout::date)
            .fixedWidth(150)
            .cellFormat { date ->
                text = date.prettyString
            }
    }
}