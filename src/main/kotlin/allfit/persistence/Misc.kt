package allfit.persistence

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
