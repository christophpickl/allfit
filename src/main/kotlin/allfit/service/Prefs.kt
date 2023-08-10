package allfit.service

import io.github.oshai.kotlinlogging.KotlinLogging.logger
import java.util.prefs.Preferences

interface Prefs {
    fun load(): PrefsData
    fun store(data: PrefsData)
}

class JavaPrefs(pathName: String) : Prefs {

    private val log = logger {}
    private val prefs = Preferences.userRoot().node(pathName)

    override fun load() = PrefsData(
        windowX = prefs.getDouble("windowX", 0.0),
        windowY = prefs.getDouble("windowY", 0.0),
        windowWidth = prefs.getDouble("windowWidth", 1200.0),
        windowHeight = prefs.getDouble("windowHeight", 700.0),
    )

    override fun store(data: PrefsData) {
        log.info { "Storing: $data" }
        prefs.putDouble("windowX", data.windowX)
        prefs.putDouble("windowY", data.windowY)
        prefs.putDouble("windowWidth", data.windowWidth)
        prefs.putDouble("windowHeight", data.windowHeight)
    }
}

data class PrefsData(
    val windowX: Double,
    val windowY: Double,
    val windowWidth: Double,
    val windowHeight: Double,
)
