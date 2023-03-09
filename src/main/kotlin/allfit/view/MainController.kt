package allfit.view

import allfit.sync.Syncer
import mu.KotlinLogging.logger
import tornadofx.Controller


class MainController : Controller() {

    private val syncer: Syncer by di()

    private val logger = logger {}

    fun login(email: String, password: String) {
        mainScope.launch {
            logger.info { "Login ..." }
            val client = OnefitClient.authenticate(email, password)
            println("Checkins: ${client.getUsage().data.check_ins}")
        }
    }

    fun sync() {
        syncer.sync()
    }
}
