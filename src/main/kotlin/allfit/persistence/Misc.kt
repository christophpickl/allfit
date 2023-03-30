package allfit.persistence

import allfit.persistence.domain.CategoriesTable
import allfit.persistence.domain.CheckinsTable
import allfit.persistence.domain.LocationsTable
import allfit.persistence.domain.PartnersCategoriesTable
import allfit.persistence.domain.PartnersTable
import allfit.persistence.domain.ReservationsTable
import allfit.persistence.domain.WorkoutsTable

val allTables = arrayOf(
    CategoriesTable,
    PartnersTable,
    PartnersCategoriesTable,
    WorkoutsTable,
    ReservationsTable,
    LocationsTable,
    CheckinsTable,
)

interface HasIntId {
    val id: Int
}

interface BaseEntity : HasIntId, Deletable

interface Deletable {
    val isDeleted: Boolean
}
