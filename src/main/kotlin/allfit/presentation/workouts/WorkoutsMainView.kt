package allfit.presentation.workouts

import allfit.Environment
import allfit.presentation.WorkoutSelectedFXEvent
import allfit.presentation.WorkoutSelectedThrough
import allfit.presentation.partners.PartnerDetailView
import allfit.presentation.view.UsageView
import allfit.service.Clock
import io.github.oshai.kotlinlogging.KotlinLogging.logger
import javafx.scene.control.Label
import javafx.scene.layout.Priority
import tornadofx.View
import tornadofx.borderpane
import tornadofx.center
import tornadofx.hbox
import tornadofx.hgrow
import tornadofx.onDoubleClick
import tornadofx.onSelectionChange
import tornadofx.right
import tornadofx.selectedItem
import tornadofx.top
import tornadofx.vbox
import tornadofx.vgrow

class WorkoutsMainView : View() {

    private val clock: Clock by di()
    private val logger = logger {}
    private val mainViewModel: WorkoutsMainModel by inject()

    private val searchView: WorkoutsSearchView by inject()
    private val usageView: UsageView by inject()
    private val partnerDetailView = PartnerDetailView(mainViewModel, WorkoutSelectedThrough.Workouts)
    private val workoutDetailView = WorkoutDetailView(mainViewModel)
    private val workoutsTable = WorkoutsTable(clock)

    init {
        mainViewModel.sortedFilteredWorkouts.bindTo(workoutsTable)
        workoutsTable.applySort()
        title = "AllFit " + (if (Environment.current == Environment.Development) " - Developer Mode" else "")

        with(workoutsTable) {
            onSelectionChange {
                logger.debug { "selection changed to: $selectedItem" }
                selectedItem?.let {
                    fire(WorkoutSelectedFXEvent(it))
                }
            }
            onDoubleClick {
                logger.debug { "double click for: $selectedItem" }
                selectedItem?.let {
                    fire(WorkoutSelectedFXEvent(it))
                }
            }
        }
    }

    override val root = borderpane {
        top {
            vgrow = Priority.NEVER
            hgrow = Priority.ALWAYS
            hbox {
                add(searchView)
                hgrow = Priority.NEVER
                add(usageView)
            }
        }
        center {
            vgrow = Priority.ALWAYS
            hgrow = Priority.ALWAYS
            add(workoutsTable)
        }
        right {
            hgrow = Priority.NEVER
            vgrow = Priority.ALWAYS
            vbox {
                add(workoutDetailView)
                add(Label()) // layout hack ;)
                add(partnerDetailView)
            }
        }
    }
}