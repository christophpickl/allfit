package allfit

import allfit.api.OnefitClient
import allfit.persistence.persistenceModule
import allfit.service.DataStorage
import org.koin.dsl.module

fun rootModule(config: AppConfig, onefitClient: OnefitClient) = module {
    single { onefitClient }
    single { DataStorage(get()) }
    single { AllFitStarter(get()) }

    includes(persistenceModule(config))
}
