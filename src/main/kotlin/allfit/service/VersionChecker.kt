package allfit.service

import io.github.oshai.kotlinlogging.KotlinLogging.logger
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.runBlocking

sealed interface VersionResult {
    companion object {
        const val SNAPSHOT_VERSION = 0
    }

    data class UpToDate(val currentVersion: Int) : VersionResult
    data class TooOld(val currentVersion: Int, val latestVersion: Int) : VersionResult
}

interface VersionChecker {
    suspend fun check(currentVersion: Int): VersionResult
}

class OnlineVersionChecker : VersionChecker {

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            runBlocking {
                println(OnlineVersionChecker().check(0))
            }
        }
    }

    private val log = logger {}
    private val versionFileUrl = "https://raw.githubusercontent.com/christophpickl/allfit/main/version.txt"

    override suspend fun check(currentVersion: Int): VersionResult {
        log.debug { "Checking version for $currentVersion ..." }
        if (currentVersion == VersionResult.SNAPSHOT_VERSION) {
            return VersionResult.UpToDate(currentVersion)
        }
        val onlineVersion = loadOnlineVersion()
        log.debug { "Online version is: $onlineVersion" }
        return if (onlineVersion > currentVersion) {
            VersionResult.TooOld(currentVersion = currentVersion, latestVersion = onlineVersion)
        } else VersionResult.UpToDate(currentVersion)
    }

    private suspend fun loadOnlineVersion(): Int {
        val response = HttpClient().get(versionFileUrl)
        response.requireOk()
        val responseText = response.bodyAsText()
        return responseText.trim().toIntOrNull() ?: error("Online version.txt file is corrupt: [$responseText]")
    }
}

object NoopVersionChecker : VersionChecker {
    override suspend fun check(currentVersion: Int) = VersionResult.UpToDate(currentVersion)
}
