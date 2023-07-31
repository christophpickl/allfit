package allfit.presentation.view

import allfit.presentation.NotesView
import allfit.presentation.partners.PartnersMainView
import allfit.presentation.setOnTabShortcutListener
import allfit.presentation.workouts.WorkoutsMainView
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
        tabPane = tabpane {
            tab("Workouts") {
                add(find<WorkoutsMainView>())
            }
            tab("Partners") {
                add(find<PartnersMainView>())
            }
            tab("Notes") {
                add(find<NotesView>())
            }
        }

        setOnTabShortcutListener(this@MainView)
    }
}

