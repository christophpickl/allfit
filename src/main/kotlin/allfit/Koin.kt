package allfit

import allfit.api.OnefitClient
import allfit.persistence.CategoriesRepo
import allfit.persistence.InMemoryCategoriesRepo
import allfit.sync.RealSyncer
import allfit.sync.Syncer
import org.koin.dsl.module

fun mainModule(onefitClient: OnefitClient) = module {
    single { onefitClient }
    single<CategoriesRepo> { InMemoryCategoriesRepo }
    single<Syncer> { RealSyncer(get(), get()) }
//    single<Syncer> { NoOpSyncer }
    single { AllFitStarter() }
}
