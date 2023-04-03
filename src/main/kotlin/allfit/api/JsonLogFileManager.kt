package allfit.api

import allfit.service.DirectoryEntry
import allfit.service.FileResolver
import allfit.service.formatFileSafe
import mu.KotlinLogging.logger
import java.io.File
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

fun main() {
    JsonLogFileManagerImpl().deleteOldLogs()
}

interface JsonLogFileManager {
    fun save(file: JsonLogFileName, jsonString: String)
    fun deleteOldLogs()
}

object NoOpJsonLogFileManager : JsonLogFileManager {
    override fun save(file: JsonLogFileName, jsonString: String) {
    }

    override fun deleteOldLogs() {
    }
}

class JsonLogFileManagerImpl : JsonLogFileManager {

    private val log = logger {}

    override fun save(file: JsonLogFileName, jsonString: String) {
        File(FileResolver.resolve(DirectoryEntry.JsonLogs), file.fileName).writeText(jsonString)
    }

    override fun deleteOldLogs() {
        log.info { "Deleting old log files..." }
        val now = LocalDateTime.now()
        FileResolver.resolve(DirectoryEntry.JsonLogs).listFiles()!!.toList().forEach {
            log.trace { "Checking log file: ${it.name}" }
            if (it.name.endsWith(".json") && it.calculateDaysDiff(now) >= 3) {
                log.debug { "Deleting old json log file: ${it.name}" }
                it.delete()
            }
        }
    }

    private fun File.calculateDaysDiff(reference: LocalDateTime) =
        ChronoUnit.DAYS.between(JsonLogFileName.parseDate(name), reference)

}

data class JsonLogFileName(
    val path: String,
    val status: Int,
    val date: ZonedDateTime,
) {
    val fileName = "${path.replace("/", "_")}-$status-${date.formatFileSafe()}.json"

    companion object {
        fun parseDate(fileName: String): LocalDateTime {
            val parts = fileName.substringBeforeLast(".").split("-")
            val dateParts = parts[2].split("_")
            val timeParts = parts[3].split("_")
            return LocalDateTime.of(
                dateParts[0].toInt(), dateParts[1].toInt(), dateParts[2].toInt(),
                timeParts[0].toInt(), timeParts[1].toInt(), timeParts[2].toInt(),
            )
        }
    }
}
