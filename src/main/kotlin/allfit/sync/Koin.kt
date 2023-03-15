package allfit.sync

import allfit.AppConfig
import org.koin.dsl.module

fun syncModule(config: AppConfig) = module {
    single {
        if (config.mockSyncer) NoOpSyncer else RealSyncer(get(), get(), get(), get(), get(), get(), get(), get())
    }
    single<WorkoutFetcher> { WorkoutFetcherImpl() }
}
