package allfit.presentation.view

import allfit.Environment
import allfit.presentation.WorkoutSelectedFXEvent
import allfit.presentation.models.MainViewModel
import allfit.presentation.partnersview.PartnersWindow
import allfit.service.Clock
import javafx.scene.control.Label
import javafx.scene.layout.Priority
import javafx.stage.Modality
import tornadofx.View
import tornadofx.action
import tornadofx.borderpane
import tornadofx.center
import tornadofx.hgrow
import tornadofx.item
import tornadofx.menu
import tornadofx.menubar
import tornadofx.onDoubleClick
import tornadofx.onSelectionChange
import tornadofx.right
import tornadofx.selectedItem
import tornadofx.top
import tornadofx.vbox
import tornadofx.vgrow

class MainView : View() {

    private val clock: Clock by di()
    private val searchView: SearchView by inject()
    private val partnersWindow: PartnersWindow by inject()
    private val partnerDetailView: PartnerDetailView by inject()
    private val workoutDetailView: WorkoutDetailView by inject()
    private val mainViewModel: MainViewModel by inject()
    private val workoutsTable = WorkoutsTable(clock)

    init {
        mainViewModel.sortedFilteredWorkouts.bindTo(workoutsTable)
        workoutsTable.applySort()
        title = "AllFit " + (if (Environment.current == Environment.Development) " - DEV" else "")

        with(workoutsTable) {
            onSelectionChange {
                selectedItem?.let {
                    fire(WorkoutSelectedFXEvent(it))
                }
            }
            onDoubleClick {
                selectedItem?.let {
                    fire(WorkoutSelectedFXEvent(it))
                }
            }
        }
    }

    override val root = vbox {
        menubar {
            menu("View") {
                item("Partners", "Shortcut+P").action {
                    partnersWindow.openModal(modality = Modality.NONE, resizable = true)
                }
            }
        }
        borderpane {
            top {
                vgrow = Priority.NEVER
                add(searchView)
            }
            center {
                vgrow = Priority.ALWAYS
                borderpane {
                    center {
                        hgrow = Priority.ALWAYS
                        add(workoutsTable)
                    }
                    right {
                        hgrow = Priority.NEVER
                        vbox {
                            add(workoutDetailView)
                            add(Label()) // layout hack ;)
                            add(partnerDetailView)
                        }
                    }
                }
            }
        }
    }
}
