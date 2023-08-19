package allfit.presentation.view

import allfit.Environment
import allfit.presentation.NotesView
import allfit.presentation.partners.PartnersMainView
import allfit.presentation.setOnTabShortcutListener
import allfit.presentation.workouts.WorkoutsMainView
import allfit.service.MetaProps
import javafx.scene.control.TabPane
import tornadofx.View
import tornadofx.singleAssign
import tornadofx.tab
import tornadofx.tabpane
import tornadofx.vbox

class MainView : View() {

    var tabPane by singleAssign<TabPane>()

    override val root = vbox {
//        menubar {
//            menu("View") {
//                item("Maximize Window", "Shortcut+M").action {
//                    maximizeWindow()
//                }
//            }
//        }

        title =
            "AllFit v${MetaProps.instance.version}" + (if (Environment.current == Environment.Development) " - Developer Mode" else "")

        tabPane = tabpane {
            tab("Workouts (1)") {
                add(find<WorkoutsMainView>())
            }
            tab("Partners (2)") {
                add(find<PartnersMainView>())
            }
            tab("Notes (3)") {
                add(find<NotesView>())
            }
        }

        setOnTabShortcutListener(this@MainView)
    }
}

