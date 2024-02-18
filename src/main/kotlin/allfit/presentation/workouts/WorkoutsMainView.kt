package allfit.presentation.workouts

import allfit.presentation.WorkoutSelectedFXEvent
import allfit.presentation.WorkoutSelectedThrough
import allfit.presentation.partners.PartnerDetailView
import allfit.presentation.tornadofx.setAllWidths
import allfit.presentation.view.UsageView
import allfit.service.Clock
import io.github.oshai.kotlinlogging.KotlinLogging.logger
import javafx.scene.layout.Priority
import tornadofx.View
import tornadofx.borderpane
import tornadofx.center
import tornadofx.hbox
import tornadofx.hgrow
import tornadofx.onDoubleClick
import tornadofx.onSelectionChange
import tornadofx.paddingAll
import tornadofx.paddingLeft
import tornadofx.paddingRight
import tornadofx.right
import tornadofx.selectedItem
import tornadofx.top
import tornadofx.vbox
import tornadofx.vgrow

class WorkoutsMainView : View() {

    private val clock: Clock by di()
    private val logger = logger {}
    private val workoutsModel: WorkoutsViewModel by inject()

    private val searchView: WorkoutsSearchView by inject()
    private val usageView: UsageView by inject()
    private val partnerDetailView = PartnerDetailView(workoutsModel.selectedPartner, WorkoutSelectedThrough.Workouts)
    private val workoutDetailView = WorkoutDetailView(workoutsModel.selectedWorkout)
    private val workoutsTable = WorkoutsTable(clock)

    init {
        workoutsModel.sortedFilteredWorkouts.bindTo(workoutsTable)
        workoutsTable.applySort()

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
            hbox(spacing = 30.0) {
                paddingAll = 10.0
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
            vbox(spacing = 10.0) {
                setAllWidths(700.0)
                paddingLeft = 5.0
                paddingRight = 5.0
                add(workoutDetailView)
                add(partnerDetailView)
            }
        }
    }
}