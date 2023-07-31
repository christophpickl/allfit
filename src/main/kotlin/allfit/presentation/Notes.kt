package allfit.presentation

import allfit.presentation.tornadofx.safeSubscribe
import allfit.service.Prefs
import javafx.beans.property.SimpleStringProperty
import tornadofx.Controller
import tornadofx.View
import tornadofx.ViewModel
import tornadofx.textarea

class NotesController : Controller() {

    private val model by inject<NotesModel>()
    private val prefs by di<Prefs>()

    init {
        safeSubscribe<ApplicationStartedFxEvent>() {
            model.notes.set(prefs.loadNotes())
        }
        safeSubscribe<ApplicationStoppingFxEvent>() {
            prefs.storeNotes(model.notes.get())
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
