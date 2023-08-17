package allfit.service

sealed interface VersionResult {
    class UpToDate(val currentVersion: Int) : VersionResult
    class TooOld(val currentVersion: Int, val latestVersion: Int) : VersionResult
}

interface VersionChecker {
    fun check(currentVersion: Int): VersionResult
}

class OnlineVersionChecker : VersionChecker {
    override fun check(currentVersion: Int): VersionResult {
        // FIXME implement me
        return VersionResult.UpToDate(currentVersion)
    }
}
