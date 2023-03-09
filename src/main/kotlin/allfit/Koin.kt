package allfit

import allfit.api.OnefitClient
import allfit.persistence.CategoryDbo
import allfit.persistence.ExposedCategoriesRepo
import allfit.persistence.ExposedPartnersRepo
import allfit.persistence.InMemoryCategoriesRepo
import allfit.persistence.InMemoryPartnersRepo
import allfit.persistence.LiquibaseConfig
import allfit.persistence.LiquibaseMigrator
import allfit.persistence.PartnerDbo
import allfit.service.DataStorage
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
    single { DataStorage(get()) }
    single { onefitClient }
    single {
        if (AppConfig.mockDb) InMemoryCategoriesRepo().apply {
            insert(
                listOf(
                    CategoryDbo(id = 1, name = "Foo", isDeleted = false),
                    CategoryDbo(id = 2, name = "Bar", isDeleted = false)
                )
            )
        } else ExposedCategoriesRepo
    }
    single {
        if (AppConfig.mockDb) InMemoryPartnersRepo().apply {
            insert(
                listOf(
                    PartnerDbo(id = 1, name = "Partner A", isDeleted = false),
                    PartnerDbo(id = 2, name = "Partner B", isDeleted = false)
                )
            )
        } else ExposedPartnersRepo
    }
    single {
        if (AppConfig.mockSyncer) NoOpSyncer else RealSyncer(get(), get(), get())
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
