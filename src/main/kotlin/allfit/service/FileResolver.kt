package allfit.service

import allfit.Environment
import mu.KotlinLogging.logger
import java.io.File

private val log = logger {}

object FileResolver {

    private val homeDirectory = File(System.getProperty("user.home"))
    private val appDirectoryProd = File(homeDirectory, ".allfit")
    private val appDirectoryDev = File(homeDirectory, ".allfit-dev")
    private val appDirectory = when (Environment.current) {
        Environment.Production -> appDirectoryProd
        Environment.Development -> appDirectoryDev
    }

    init {
        appDirectoryProd.createIfNeededOrFail()
        appDirectoryDev.createIfNeededOrFail()
    }

    private fun baseDirectory(entry: DiskEntry) =
        if (entry.environmentAgnostic) appDirectoryProd else appDirectory

    fun resolve(entry: FileEntry) = File(baseDirectory(entry), entry.fileName)
    fun resolve(entry: DirectoryEntry) = File(baseDirectory(entry), entry.directoryName).createIfNeededOrFail()
}

private fun File.createIfNeededOrFail() = apply {
    if (!exists()) {
        log.debug { "Creating directory at: $absolutePath" }
        if (!mkdir()) {
            error("Could not create directory at: $absolutePath")
        }
    }
}

interface DiskEntry {
    val environmentAgnostic: Boolean
}

enum class FileEntry(val fileName: String, override val environmentAgnostic: Boolean = false) : DiskEntry {
    Login("login.json"),
}

enum class DirectoryEntry(val directoryName: String, override val environmentAgnostic: Boolean = false) : DiskEntry {
    Database("database"),
    JsonLogs("json_logs"),
    ImagesPartners("images/partners", environmentAgnostic = true),
    ImagesWorkouts("images/workouts", environmentAgnostic = true),
}
