package allfit.service

import mu.KotlinLogging
import java.io.File

object FileStorage {
    private val log = KotlinLogging.logger {}
    private val appDirectory = File(System.getProperty("user.home"), ".allfit")

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