package allfit.presentation

import allfit.persistence.domain.SinglesRepo
import allfit.presentation.tornadofx.safeSubscribe
import javafx.beans.property.SimpleStringProperty
import tornadofx.Controller
import tornadofx.View
import tornadofx.ViewModel
import tornadofx.textarea

class NotesController : Controller() {

    private val model by inject<NotesModel>()
    private val singlesRepo by di<SinglesRepo>()

    init {
        safeSubscribe<ApplicationStartedFxEvent>() {
            model.notes.set(singlesRepo.selectNotes())
        }
        safeSubscribe<ApplicationStoppingFxEvent>() {
            singlesRepo.updateNotes(model.notes.get())
        }
    }
}

class NotesModel : ViewModel() {
    val notes = SimpleStringProperty()
}

class NotesView : View() {

    private val model by inject<NotesModel>()

    override val root = textarea {
        textProperty().bindBidirectional(model.notes)
        setOnTabShortcutListener(this@NotesView)
    }
}
