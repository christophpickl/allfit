package allfit.presentation.view

import allfit.Environment
import allfit.presentation.WorkoutSelectedFXEvent
import allfit.presentation.models.MainViewModel
import javafx.scene.layout.Priority
import tornadofx.View
import tornadofx.borderpane
import tornadofx.center
import tornadofx.hgrow
import tornadofx.onSelectionChange
import tornadofx.right
import tornadofx.selectedItem
import tornadofx.top
import tornadofx.vbox
import tornadofx.vgrow

class MainView : View() {

    private val searchView: SearchView by inject()
    private val partnerDetailView: PartnerDetailView by inject()
    private val workoutDetailView: WorkoutDetailView by inject()
    private val mainViewModel: MainViewModel by inject()
    private val workoutsTable = WorkoutsTable(mainViewModel.allWorkouts)

    init {
        title = "AllFit " + (if (Environment.current == Environment.Development) " - DEV" else "")

        with(workoutsTable) {
            onSelectionChange {
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
                        add(partnerDetailView)
                        add(workoutDetailView)
                    }
                }
            }
        }
    }
}
