package allfit.sync

import allfit.AppConfig
import org.koin.dsl.module

fun syncModule(config: AppConfig) = module {
    single<CategoriesSyncer> { CategoriesSyncerImpl(get(), get()) }
    single<PartnersSyncer> { PartnersSyncerImpl(get(), get(), get()) }
    single<LocationsSyncer> { LocationsSyncerImpl(get()) }
    single<WorkoutsSyncer> { WorkoutsSyncerImpl(get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single<ReservationsSyncer> { ReservationsSyncerImpl(get(), get(), get()) }
    single<CheckinsSyncer> { CheckinsSyncerImpl(get(), get(), get(), get(), get(), get()) }
    single<Syncer> {
        if (config.mockSyncer) NoOpSyncer(get()) else CompositeSyncer(
            get(), get(), get(), get(), get(), get(), get(), get(), get(),
        )
    }
    single<SyncListenerManager> { SyncListenerManagerImpl() }
    single<WorkoutFetcher> { WorkoutFetcherImpl() }
}
