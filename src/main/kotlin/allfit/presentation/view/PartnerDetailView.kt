package allfit.presentation.view

import allfit.presentation.PartnerModifications
import allfit.presentation.SavePartnerFXEvent
import allfit.presentation.models.MainViewModel
import javafx.beans.value.ObservableValue
import javafx.geometry.Orientation
import javafx.scene.control.CheckBox
import javafx.scene.control.TextField
import tornadofx.View
import tornadofx.action
import tornadofx.bind
import tornadofx.button
import tornadofx.checkbox
import tornadofx.enableWhen
import tornadofx.field
import tornadofx.fieldset
import tornadofx.filterInput
import tornadofx.form
import tornadofx.isInt
import tornadofx.label
import tornadofx.singleAssign
import tornadofx.textfield
import tornadofx.vbox

class PartnerDetailView : View() {

    private val mainViewModel: MainViewModel by inject()
    private var ratingInput: TextField by singleAssign()
    private var favouriteCheckbox: CheckBox by singleAssign()
    private var wishlistCheckbox: CheckBox by singleAssign()

    private val enabledChecker: () -> ObservableValue<Boolean> = {
        mainViewModel.selectedPartner.id.greaterThan(0)
    }
    override val root = vbox {
        label("Partner Detail")
        form {
            fieldset(labelPosition = Orientation.HORIZONTAL) {
                field("Name:") {
                    label().bind(mainViewModel.selectedPartner.name)
                }
                field("Rating:") {
                    ratingInput = textfield {
                        bind(mainViewModel.selectedPartner.rating)
                        enableWhen(enabledChecker)
                        filterInput { it.controlNewText.isInt() && it.controlNewText.toInt() < 6 }
                        prefWidth = 30.0
                    }
                }
                field("Favourite:") {
                    favouriteCheckbox = checkbox {
                        bind(mainViewModel.selectedPartner.isFavourited)
                        enableWhen(enabledChecker)
                    }
                }
                field("Wishlist:") {
                    wishlistCheckbox = checkbox {
                        bind(mainViewModel.selectedPartner.isWishlisted)
                        enableWhen(enabledChecker)
                    }
                }
            }
        }
        button("Save Changes") {
            enableWhen(enabledChecker)
            action {
                fire(
                    SavePartnerFXEvent(
                        PartnerModifications(
                            partnerId = mainViewModel.selectedPartner.id.get(),
                            rating = ratingInput.text.toIntOrNull()
                                ?: error("Invalid rating input: '${ratingInput.text}'"),
                            isFavourited = favouriteCheckbox.isSelected,
                            isWishlisted = wishlistCheckbox.isSelected,
                        )
                    )
                )
            }
        }
    }
}
