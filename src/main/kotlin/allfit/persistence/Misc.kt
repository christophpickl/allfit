package allfit.persistence

import allfit.domain.Deletable
import allfit.domain.HasIntId

val allTables = arrayOf(
    CategoriesTable,
    PartnersTable,
    PartnersCategoriesTable,
    WorkoutsTable,
    ReservationsTable,
    LocationsTable,
    CheckinsTable,
)

interface BaseEntity : HasIntId, Deletable
