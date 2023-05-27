package allfit

import allfit.api.JsonLogFileManager
import allfit.api.JsonLogFileManagerImpl
import allfit.api.OnefitClient
import allfit.persistence.persistenceModule
import allfit.presentation.UiSyncer
import allfit.presentation.logic.ExposedDataStorage
import allfit.presentation.logic.InMemoryDataStorage
import allfit.service.*
import allfit.sync.syncModule
import org.koin.dsl.module

fun rootModule(config: AppConfig, onefitClient: OnefitClient) = module {
    single { onefitClient }
    single<Clock> {
        config.dummyDate?.let {
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
    single { if (config.mockDataStore) InMemoryDataStorage else ExposedDataStorage(get(), get()) }
    single { UiSyncer(get()) }
    single { AllFitStarter(config.syncEnabled, get()) }
    single<JsonLogFileManager> { JsonLogFileManagerImpl() }

    includes(persistenceModule(config))
    includes(syncModule())
}
