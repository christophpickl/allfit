package allfit.persistence

import allfit.domain.Deletable
import allfit.domain.HasIntId

val allTables = arrayOf(
    CategoriesTable,
    PartnersTable,
    PartnersCategoriesTable,
    WorkoutsTable,
)

interface BaseEntity : HasIntId, Deletable
