package allfit.presentation.partnersview

import allfit.presentation.PartnerSelectedFXEvent
import allfit.presentation.Styles
import allfit.presentation.models.PartnersViewModel
import allfit.presentation.models.UsageModel
import allfit.presentation.view.PartnerDetailView
import allfit.service.Clock
import javafx.scene.layout.Priority
import tornadofx.App
import tornadofx.View
import tornadofx.action
import tornadofx.borderpane
import tornadofx.center
import tornadofx.hbox
import tornadofx.hgrow
import tornadofx.item
import tornadofx.label
import tornadofx.launch
import tornadofx.menu
import tornadofx.menubar
import tornadofx.onDoubleClick
import tornadofx.onSelectionChange
import tornadofx.right
import tornadofx.selectedItem
import tornadofx.top
import tornadofx.vbox
import tornadofx.vgrow

class PartnersWindowApp : App(
    primaryView = PartnersWindow::class,
    stylesheet = Styles::class,
)

class PartnersSearchView : View() {

    override val root = hbox {

    }
}

class PartnersWindow : View() {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            launch<PartnersWindowApp>()
        }
    }

    private val clock: Clock by di()

    private val partnersViewModel: PartnersViewModel by inject()
    private val searchView: PartnersSearchView by inject()
    private val usageModel: UsageModel by inject()
    private val partnersTable = PartnersTable(usageModel.usage.get(), clock)

    private val partnerDetailView = PartnerDetailView(partnersViewModel)

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

    override val root =
        vbox {
            menubar {
                menu("View") {
                    item("Close", "Shortcut+W").action {
                        this@PartnersWindow.close()
                    }
                }
            }
            borderpane {
                top {
                    hgrow = Priority.NEVER
                    vgrow = Priority.NEVER
                    add(label("Filter"))
                }
                center {
                    hgrow = Priority.ALWAYS
                    vgrow = Priority.ALWAYS
                    add(partnersTable)
                }
                right {
                    hgrow = Priority.NEVER
                    vgrow = Priority.ALWAYS
                    add(partnerDetailView)
                }
            }
        }
}

