package allfit

import allfit.api.OnefitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private val mainScope = CoroutineScope(Dispatchers.Main)

class Controller {

    private val log = mu.KotlinLogging.logger {}

    fun login(email: String, password: String) {
        mainScope.launch {
            log.info { "Login ..." }
            val client = OnefitClient.authenticate(email, password)
            println("Checkins: ${client.getUsage().data.check_ins}")
        }
    }
}