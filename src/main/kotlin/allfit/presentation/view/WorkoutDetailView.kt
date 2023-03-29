package allfit.presentation.view

import allfit.presentation.StaticImage
import allfit.presentation.StaticImageStorage
import allfit.presentation.Styles
import allfit.presentation.models.MainViewModel
import tornadofx.View
import tornadofx.action
import tornadofx.addClass
import tornadofx.bind
import tornadofx.button
import tornadofx.imageview
import tornadofx.label
import tornadofx.textarea
import tornadofx.tooltip
import tornadofx.vbox
import tornadofx.visibleWhen
import java.awt.Desktop
import java.net.URI

class WorkoutDetailView : View() {

    private val mainViewModel: MainViewModel by inject()

    override val root = vbox {
        label("Workout") {
            bind(mainViewModel.selectedWorkout.map { it.name })
            addClass(Styles.header1)
        }
        imageview(mainViewModel.selectedWorkout.map { it.image })

//        webview {
//            prefHeight = 100.0
//            javafx.scene.

//            engine.userAgent = iPhoneUserAgent
        //bind(mainViewModel.selectedWorkout.map { it.about })
//        }
        textarea {
            bind(mainViewModel.selectedWorkout.map { it.specifics })
        }
        label().bind(mainViewModel.selectedWorkout.map { it.date.prettyString })

        imageview(StaticImageStorage.get(StaticImage.Reserved)) {
            visibleWhen {
                mainViewModel.selectedWorkout.map { it.isReserved }
            }
        }
        button("Open Website") {
            tooltip {
                this@tooltip.textProperty().bind(mainViewModel.selectedWorkout.map { it.url })
            }
            action {
                Desktop.getDesktop().browse(URI(mainViewModel.selectedWorkout.value.url))
            }
        }
    }
}
