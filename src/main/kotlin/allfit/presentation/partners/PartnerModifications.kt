package allfit.presentation.partners

import allfit.persistence.domain.PartnerEntity
import allfit.persistence.domain.PartnersTable
import allfit.presentation.models.PartnerCustomAttributesWrite
import allfit.presentation.models.Rating
import org.jetbrains.exposed.sql.statements.UpdateStatement

data class PartnerModifications(
    val partnerId: Int,
    val rating: Rating,
    val note: String,
    val officialWebsite: String?,
    val isFavorited: Boolean,
    val isWishlisted: Boolean,
) {
    fun update(partner: PartnerCustomAttributesWrite) {
        partner.rating = rating
        partner.note = note
        partner.officialWebsite = officialWebsite
        partner.isFavorited = isFavorited
        partner.isWishlisted = isWishlisted
    }

    fun prepare(new: PartnerEntity, stmt: UpdateStatement) {
        stmt[PartnersTable.rating] = new.rating
        stmt[PartnersTable.note] = new.note
        stmt[PartnersTable.officialWebsite] = new.officialWebsite
        stmt[PartnersTable.isFavorited] = new.isFavorited
        stmt[PartnersTable.isWishlisted] = new.isWishlisted
    }

    fun modify(old: PartnerEntity): PartnerEntity =
        old.copy(
            rating = rating,
            note = note,
            officialWebsite = officialWebsite,
            isFavorited = isFavorited,
            isWishlisted = isWishlisted,
        )
}