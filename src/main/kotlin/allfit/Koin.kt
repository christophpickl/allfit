package allfit

import allfit.api.JsonLogFileManager
import allfit.api.JsonLogFileManagerImpl
import allfit.api.OnefitClient
import allfit.persistence.persistenceModule
import allfit.presentation.UiPreSyncer
import allfit.presentation.logic.ExposedDataStorage
import allfit.presentation.logic.InMemoryDataStorage
import allfit.service.*
import allfit.sync.syncModule
import io.github.oshai.kotlinlogging.KotlinLogging.logger
import org.koin.dsl.module

private val log = logger {}

fun rootModule(config: AppConfig, onefitClient: OnefitClient) = module {
    single { onefitClient }
    single<Clock> {
        config.dummyDate?.let {
            log.info { "Static dummy date configured at: $it" }
            DummyDayClock(it)
        } ?: SystemClock
    }
    single<ImageStorage> {
        FileSystemImageStorage(
            partnersFolder = FileResolver.resolve(DirectoryEntry.ImagesPartners),
            syncListeners = get(),
        )
    }
    single {
        if (config.mockDataStore) InMemoryDataStorage(get()) else ExposedDataStorage(
            get(), get(), get(), get(), get(), get(), get(), get(),
        )
    }
    single { UiPreSyncer(get()) }
    single { AllFitStarter(config.preSyncEnabled, get(), get()) }
    single<JsonLogFileManager> { JsonLogFileManagerImpl() }
    single<Prefs> { JavaPrefs("allfit" + (if (config.isDevMode) "-dev" else "")) }
    single<WorkoutInserter> { WorkoutInserterImpl(get(), get()) }
    single<PartnerInserter> { PartnerInserterImpl(get(), get()) }
    single<VersionChecker> {
        if (config.mockClient) NoopVersionChecker else OnlineVersionChecker()
    }

    includes(persistenceModule(config))
    includes(syncModule(config))
}
