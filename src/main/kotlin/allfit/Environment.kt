package allfit

import mu.KotlinLogging.logger

enum class Environment {
    Production,
    Development;

    companion object {
        private val log = logger {}
        val current by lazy {
            when (System.getProperty("allift.environment", "").lowercase()) {
                "prod, production" -> Production
                else -> Development
            }.also {
                log.info { "Current environment is: $it" }
            }
        }
    }
}
