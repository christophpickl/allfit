package allfit.view

import allfit.api.OnefitClient
import allfit.sync.Syncer
import mu.KotlinLogging.logger
import tornadofx.Controller

class MainController : Controller() {

    private val syncer: Syncer by di()
    private val client: OnefitClient by di()
    private val logger = logger {}

    suspend fun sync() {
        syncer.sync()
    }
}
