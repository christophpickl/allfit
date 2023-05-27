package allfit.sync

import org.koin.dsl.module

fun syncModule() = module {
    single<CategoriesSyncer> { CategoriesSyncerImpl(get(), get()) }
    single<PartnersSyncer> { PartnersSyncerImpl(get(), get(), get()) }
    single<LocationsSyncer> { LocationsSyncerImpl(get()) }
    single<WorkoutsSyncer> { WorkoutsSyncerImpl(get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single<ReservationsSyncer> { ReservationsSyncerImpl(get(), get(), get()) }
    single<CheckinsSyncer> { CheckinsSyncerImpl(get(), get(), get(), get(), get(), get()) }
    single<Syncer> { CompositeSyncer(get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single<SyncListenerManager> { SyncListenerManagerImpl() }
    single<WorkoutFetcher> { WorkoutFetcherImpl() }
}
