package allfit

import allfit.api.OnefitClient
import allfit.persistence.persistenceModule
import allfit.presentation.logic.DataStorage
import allfit.presentation.logic.InMemoryDataStorage
import allfit.service.DirectoryEntry
import allfit.service.FileResolver
import allfit.service.FileSystemImageStorage
import allfit.service.ImageStorage
import allfit.sync.syncModule
import org.koin.dsl.module

fun rootModule(config: AppConfig, onefitClient: OnefitClient) = module {
    single { onefitClient }
    single<ImageStorage> {
        FileSystemImageStorage(
            partnersFolder = FileResolver.resolve(DirectoryEntry.ImagesPartners),
            workoutsFolder = FileResolver.resolve(DirectoryEntry.ImagesWorkouts),
        )
    }

    single<DataStorage> { InMemoryDataStorage }
    single { AllFitStarter(get()) }

    includes(persistenceModule(config))
    includes(syncModule(config))
}
