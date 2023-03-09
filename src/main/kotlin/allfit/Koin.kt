package allfit

import allfit.api.OnefitClient
import allfit.persistence.ExposedCategoriesRepo
import allfit.persistence.InMemoryCategoriesRepo
import allfit.persistence.LiquibaseConfig
import allfit.persistence.LiquibaseMigrator
import allfit.service.FileResolver
import allfit.sync.NoOpSyncer
import allfit.sync.RealSyncer
import mu.KotlinLogging.logger
import org.jetbrains.exposed.sql.Database
import org.koin.dsl.module

private val log = logger {}

fun mainModule(onefitClient: OnefitClient) = module {
    if (!AppConfig.mockDb) {
        connectToDatabase()
    }
    single { onefitClient }
    single {
        if (AppConfig.mockDb) InMemoryCategoriesRepo else ExposedCategoriesRepo
    }
    single {
        if (AppConfig.mockSyncer) NoOpSyncer else RealSyncer(get(), get())
    }
    single { AllFitStarter(get()) }
}

private fun connectToDatabase() {
    val dbDir = FileResolver.resolve("database")
    val jdbcUrl = "jdbc:h2:file:${dbDir.absolutePath}"
    log.debug { "Connecting to database: $jdbcUrl" }
    LiquibaseMigrator.migrate(LiquibaseConfig("", "", jdbcUrl))
    Database.connect(jdbcUrl)

}
