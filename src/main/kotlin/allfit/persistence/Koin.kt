package allfit.persistence

import allfit.AppConfig
import allfit.service.DirectoryEntry
import allfit.service.FileResolver
import mu.KotlinLogging.logger
import org.jetbrains.exposed.sql.Database
import org.koin.dsl.module

private val log = logger {}

fun persistenceModule(config: AppConfig) = module {
    if (!config.mockDb) {
        connectToDatabase()
    }
    single {
        if (config.mockDb) InMemoryCategoriesRepo().insertMockData() else ExposedCategoriesRepo
    }
    single {
        if (config.mockDb) InMemoryPartnersRepo().insertMockData() else ExposedPartnersRepo
    }
    single {
        if (config.mockDb) InMemoryWorkoutsRepo().insertMockData() else ExposedWorkoutsRepo
    }
    single {
        if (config.mockDb) InMemoryReservationsRepo().insertMockData() else ExposedReservationsRepo
    }
    single {
        if (config.mockDb) InMemoryReservationsRepo().insertMockData() else ExposedReservationsRepo
    }
    single {
        if (config.mockDb) InMemoryLocationsRepo().insertMockData() else ExposedLocationsRepo
    }
}

private fun connectToDatabase() {
    val dbDir = FileResolver.resolve(DirectoryEntry.Database)
    val jdbcUrl = "jdbc:h2:file:${dbDir.absolutePath}/h2"
    log.info { "Connecting to database: $jdbcUrl" }
    LiquibaseMigrator.migrate(LiquibaseConfig("", "", jdbcUrl))
    Database.connect(jdbcUrl)
}
