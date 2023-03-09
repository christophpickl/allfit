package allfit

import allfit.sync.NoOpSyncer
import allfit.sync.Syncer
import org.koin.dsl.module

fun mainModule(cliArgs: Array<String>) = module {
    single<Syncer> { NoOpSyncer }
    single { AllFitStarter(cliArgs) }
}
