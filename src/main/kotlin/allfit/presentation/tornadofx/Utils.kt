package allfit.presentation.tornadofx

import io.github.oshai.kotlinlogging.KotlinLogging.logger
import javafx.scene.input.Clipboard
import javafx.scene.input.ClipboardContent

private val log = logger {}

fun copyToClipboard(text: String) {
    log.debug { "Copying to clipboard: [$text]" }
    Clipboard.getSystemClipboard().setContent(ClipboardContent().apply { putString(text) })
}
