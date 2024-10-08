package allfit.sync

import allfit.AppConfig
import allfit.sync.core.CompositeOnefitSyncer
import allfit.sync.core.NoOpSyncer
import allfit.sync.core.SyncListenerManager
import allfit.sync.core.SyncListenerManagerImpl
import allfit.sync.core.Syncer
import allfit.sync.domain.CategoriesSyncer
import allfit.sync.domain.CategoriesSyncerImpl
import allfit.sync.domain.CheckinsSyncer
import allfit.sync.domain.CheckinsSyncerImpl
import allfit.sync.domain.HttpWorkoutMetadataFetcher
import allfit.sync.domain.LocationsSyncer
import allfit.sync.domain.LocationsSyncerImpl
import allfit.sync.domain.PartnersSyncer
import allfit.sync.domain.PartnersSyncerImpl
import allfit.sync.domain.ReservationsSyncer
import allfit.sync.domain.ReservationsSyncerImpl
import allfit.sync.domain.WorkoutMetadataFetcher
import allfit.sync.domain.WorkoutsSyncer
import allfit.sync.domain.WorkoutsSyncerImpl
import allfit.sync.presync.ApiPreSyncer
import allfit.sync.presync.PreSyncListenerManager
import allfit.sync.presync.PreSyncListenerManagerImpl
import allfit.sync.presync.PreSyncer
import org.koin.dsl.module

fun syncModule(config: AppConfig) = module {
    single<CategoriesSyncer> { CategoriesSyncerImpl(get(), get(), get()) }
    single<PartnersSyncer> { PartnersSyncerImpl(get(), get(), get(), get()) }
    single<LocationsSyncer> { LocationsSyncerImpl(get(), get()) }
    single<WorkoutsSyncer> { WorkoutsSyncerImpl(get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single<ReservationsSyncer> { ReservationsSyncerImpl(get(), get(), get(), get(), get(), get(), get(), get()) }
    single<CheckinsSyncer> { CheckinsSyncerImpl(get(), get(), get(), get(), get(), get(), get(), get()) }
    single<SyncListenerManager> { SyncListenerManagerImpl() }
    single<Syncer> {
        if (config.syncEnabled) {
            CompositeOnefitSyncer(get(), get(), get(), get(), get(), get(), get(), get(), get(), get())
        } else {
//            DelayedSyncer(get())
            NoOpSyncer(get())
        }
    }
    single<PreSyncListenerManager> { PreSyncListenerManagerImpl() }
    single<PreSyncer> {
        ApiPreSyncer(get(), get(), get())
    }
    single<WorkoutMetadataFetcher> { HttpWorkoutMetadataFetcher() }
}
