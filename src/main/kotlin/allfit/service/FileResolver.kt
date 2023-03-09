package allfit.service

import allfit.Environment
import mu.KotlinLogging.logger
import java.io.File

object FileResolver {

    private val log = logger {}

    private val appDirectory = File(
        System.getProperty("user.home"), when (Environment.current) {
            Environment.Production -> ".allfit"
            Environment.Development -> ".allfit-dev"
        }
    )

    init {
        if (!appDirectory.exists()) {
            log.info { "Creating app directory at: ${appDirectory.absolutePath}" }
            if (!appDirectory.mkdir()) {
                error("Could not create directory: ${appDirectory.absolutePath}")
            }
        }
    }

    fun resolve(fileName: String) = File(appDirectory, fileName)
}
