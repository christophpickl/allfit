package allfit.presentation.models

import javafx.collections.FXCollections
import tornadofx.SortedFilteredList
import tornadofx.ViewModel

class PartnersViewModel : ViewModel() {
    val allRawPartners = FXCollections.observableArrayList<FullPartner>()!!
    val allPartners = SortedFilteredList<FullPartner>(allRawPartners)
}
