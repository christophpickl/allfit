package allfit.persistence

import allfit.AppConfig
import allfit.persistence.domain.ExposedCategoriesRepo
import allfit.persistence.domain.ExposedCheckinsRepository
import allfit.persistence.domain.ExposedLocationsRepo
import allfit.persistence.domain.ExposedPartnersRepo
import allfit.persistence.domain.ExposedReservationsRepo
import allfit.persistence.domain.ExposedSinglesRepo
import allfit.persistence.domain.ExposedUsageRepo
import allfit.persistence.domain.ExposedWorkoutsRepo
import allfit.persistence.domain.InMemoryCategoriesRepo
import allfit.persistence.domain.InMemoryCheckinsRepository
import allfit.persistence.domain.InMemoryLocationsRepo
import allfit.persistence.domain.InMemoryPartnersRepo
import allfit.persistence.domain.InMemoryReservationsRepo
import allfit.persistence.domain.InMemorySinglesRepo
import allfit.persistence.domain.InMemoryUsageRepository
import allfit.persistence.domain.InMemoryWorkoutsRepo
import allfit.persistence.domain.MockDateAwareUsageRepository
import allfit.persistence.domain.UsageRepository
import allfit.service.DirectoryEntry
import allfit.service.FileResolver
import io.github.oshai.kotlinlogging.KotlinLogging.logger
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.DatabaseConfig
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
        if (config.mockDb) InMemoryReservationsRepo().insertMockData(config.dummyDate) else ExposedReservationsRepo
    }
    single {
        if (config.mockDb) InMemoryLocationsRepo().insertMockData() else ExposedLocationsRepo
    }
    single {
        if (config.mockDb) InMemoryCheckinsRepository().insertMockData() else ExposedCheckinsRepository
    }
    single {
        if (config.mockDb) InMemorySinglesRepo() else ExposedSinglesRepo
    }
    single<UsageRepository> {
        MockDateAwareUsageRepository(
            if (config.mockDb) InMemoryUsageRepository().insertMockData() else ExposedUsageRepo,
            config.dummyDate
        )
    }
}

private fun connectToDatabase() {
    val dbDir = FileResolver.resolve(DirectoryEntry.Database)
    val jdbcUrl = "jdbc:h2:file:${dbDir.absolutePath}/h2"
    log.info { "Connecting to database: $jdbcUrl" }
    LiquibaseMigrator.migrate(LiquibaseConfig("", "", jdbcUrl))
    Database.connect(jdbcUrl, databaseConfig = DatabaseConfig {
        defaultRepetitionAttempts = 1
    })
}
