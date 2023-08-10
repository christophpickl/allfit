package allfit.presentation.logic

import io.github.oshai.kotlinlogging.KotlinLogging.logger
import java.awt.Desktop
import java.net.URI

private val log = logger {}

fun openBrowser(url: String) {
    log.info { "open browser for URL: [$url]" }
    Desktop.getDesktop().browse(URI(url))
}
