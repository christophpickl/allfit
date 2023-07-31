package allfit.presentation.partners

import allfit.presentation.PartnerSelectedFXEvent
import allfit.presentation.Styles
import allfit.presentation.WorkoutSelectedThrough
import allfit.presentation.models.PartnersViewModel
import allfit.presentation.models.UsageModel
import allfit.presentation.workouts.WorkoutDetailView
import allfit.service.Clock
import javafx.scene.control.Label
import javafx.scene.layout.Priority
import tornadofx.App
import tornadofx.View
import tornadofx.borderpane
import tornadofx.center
import tornadofx.hgrow
import tornadofx.launch
import tornadofx.onDoubleClick
import tornadofx.onSelectionChange
import tornadofx.right
import tornadofx.selectedItem
import tornadofx.top
import tornadofx.vbox
import tornadofx.vgrow

class PartnersMainViewApp : App(
    primaryView = PartnersMainView::class,
    stylesheet = Styles::class,
)

class PartnersMainView : View() {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            launch<PartnersMainViewApp>()
        }
    }

    private val clock: Clock by di()

    private val partnersViewModel: PartnersViewModel by inject()
    private val searchView: PartnersSearchView by inject()
    private val usageModel: UsageModel by inject()
    private val partnersTable = PartnersTable(usageModel.usage.get(), clock)
    private val workoutDetailView = WorkoutDetailView(partnersViewModel)
    private val partnerDetailView = PartnerDetailView(partnersViewModel, WorkoutSelectedThrough.Partners)

    init {
        title = "Partners"
        partnersViewModel.sortedFilteredPartners.bindTo(partnersTable)
        partnersTable.applySort()

        with(partnersTable) {
            onSelectionChange {
                selectedItem?.let {
                    fire(PartnerSelectedFXEvent(it.id))
                }
            }
            onDoubleClick {
                selectedItem?.let {
                    fire(PartnerSelectedFXEvent(it.id))
                }
            }
        }
    }

    override val root = borderpane {
        top {
            hgrow = Priority.NEVER
            vgrow = Priority.NEVER
            add(searchView)
        }
        center {
            hgrow = Priority.ALWAYS
            vgrow = Priority.ALWAYS
            add(partnersTable)
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

