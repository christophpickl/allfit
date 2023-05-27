package allfit.sync

import allfit.AppConfig
import allfit.sync.presync.NoOpPreSyncer
import allfit.sync.presync.PreSyncListenerManager
import allfit.sync.presync.PreSyncListenerManagerImpl
import allfit.sync.presync.PreSyncer
import org.koin.dsl.module

fun syncModule(config: AppConfig) = module {
    single<CategoriesSyncer> { CategoriesSyncerImpl(get(), get()) }
    single<PartnersSyncer> { PartnersSyncerImpl(get(), get(), get()) }
    single<LocationsSyncer> { LocationsSyncerImpl(get()) }
    single<WorkoutsSyncer> { WorkoutsSyncerImpl(get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single<ReservationsSyncer> { ReservationsSyncerImpl(get(), get(), get()) }
    single<CheckinsSyncer> { CheckinsSyncerImpl(get(), get(), get(), get(), get(), get()) }
    single<SyncListenerManager> { SyncListenerManagerImpl() }
    single<Syncer> {
        if (config.syncEnabled) {
            CompositeOnefitSyncer(get(), get(), get(), get(), get(), get(), get(), get(), get())
        } else {
//            DelayedSyncer(get())
            NoOpSyncer(get())
        }
    }
    single<PreSyncListenerManager> { PreSyncListenerManagerImpl() }
    single<PreSyncer> {
        NoOpPreSyncer(get())
    }
    single<WorkoutFetcher> { WorkoutFetcherImpl() }
}
