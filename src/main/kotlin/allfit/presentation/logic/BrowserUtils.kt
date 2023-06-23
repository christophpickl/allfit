package allfit.presentation.logic

import java.awt.Desktop
import java.net.URI
import mu.KotlinLogging.logger

private val log = logger {}

fun openBrowser(url: String) {
    log.info { "open browser for URL: [$url]" }
    Desktop.getDesktop().browse(URI(url))
}
