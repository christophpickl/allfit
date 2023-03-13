package allfit

import allfit.api.OnefitClient
import allfit.persistence.persistenceModule
import allfit.service.DataStorage
import allfit.sync.NoOpSyncer
import allfit.sync.RealSyncer
import org.koin.dsl.module

fun rootModule(config: AppConfig, onefitClient: OnefitClient) = module {
    single { onefitClient }
    single {
        if (config.mockSyncer) NoOpSyncer else RealSyncer(get(), get(), get(), get(), get(), get())
    }
    single { DataStorage(get()) }
    single { AllFitStarter(get()) }

    includes(persistenceModule(config))
}
