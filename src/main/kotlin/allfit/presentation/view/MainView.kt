package allfit.presentation.view

import allfit.Environment
import allfit.presentation.WorkoutSelectedFXEvent
import allfit.presentation.models.MainViewModel
import javafx.scene.control.Label
import javafx.scene.layout.Priority
import tornadofx.*

class MainView : View() {

    private val searchView: SearchView by inject()
    private val partnerDetailView: PartnerDetailView by inject()
    private val workoutDetailView: WorkoutDetailView by inject()
    private val mainViewModel: MainViewModel by inject()
    private val workoutsTable = WorkoutsTable()

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

    override val root = borderpane {
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
