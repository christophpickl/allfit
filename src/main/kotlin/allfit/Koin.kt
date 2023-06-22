package allfit

import allfit.api.JsonLogFileManager
import allfit.api.JsonLogFileManagerImpl
import allfit.api.OnefitClient
import allfit.persistence.persistenceModule
import allfit.presentation.UiPreSyncer
import allfit.presentation.logic.ExposedDataStorage
import allfit.presentation.logic.InMemoryDataStorage
import allfit.service.Clock
import allfit.service.DirectoryEntry
import allfit.service.DummyDayClock
import allfit.service.FileResolver
import allfit.service.FileSystemImageStorage
import allfit.service.ImageStorage
import allfit.service.JavaPrefs
import allfit.service.Prefs
import allfit.service.SystemClock
import allfit.sync.syncModule
import mu.KotlinLogging
import org.koin.dsl.module

private val log = KotlinLogging.logger {}

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
            workoutsFolder = FileResolver.resolve(DirectoryEntry.ImagesWorkouts),
            syncListeners = get(),
        )
    }
    single {
        if (config.mockDataStore) InMemoryDataStorage(get()) else ExposedDataStorage(
            get(), get(), get(), get(), get(), get(), get()
        )
    }
    single { UiPreSyncer(get()) }
    single { AllFitStarter(config.preSyncEnabled, get()) }
    single<JsonLogFileManager> { JsonLogFileManagerImpl() }
    single<Prefs> { JavaPrefs("allfit" + (if (config.isDevMode) "-dev" else "")) }

    includes(persistenceModule(config))
    includes(syncModule(config))
}
