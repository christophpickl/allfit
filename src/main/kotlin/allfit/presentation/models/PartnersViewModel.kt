package allfit.presentation.models

import allfit.presentation.view.PartnerDetailModel
import javafx.collections.FXCollections
import tornadofx.SortedFilteredList
import tornadofx.ViewModel

class PartnersViewModel : ViewModel(), PartnerDetailModel {
    val allPartners = FXCollections.observableArrayList<FullPartner>()!!
    val sortedFilteredPartners = SortedFilteredList<FullPartner>(allPartners)
    override val selectedPartner = CurrentPartnerViewModel()
}
