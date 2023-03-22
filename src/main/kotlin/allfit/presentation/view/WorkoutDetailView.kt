package allfit.presentation.view

import allfit.presentation.models.MainViewModel
import tornadofx.View
import tornadofx.bind
import tornadofx.label
import tornadofx.vbox

class WorkoutDetailView : View() {

    private val mainViewModel: MainViewModel by inject()

    override val root = vbox {
        label("Workout Detail:")
        label().bind(mainViewModel.selectedWorkout.map { it.name })
    }
}