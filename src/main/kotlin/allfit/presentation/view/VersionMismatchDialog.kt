package allfit.presentation.view

import allfit.presentation.Styles
import allfit.presentation.tornadofx.link
import javafx.geometry.Pos
import javafx.scene.layout.Priority
import tornadofx.View
import tornadofx.action
import tornadofx.addClass
import tornadofx.box
import tornadofx.button
import tornadofx.hbox
import tornadofx.hgrow
import tornadofx.label
import tornadofx.px
import tornadofx.style
import tornadofx.vbox

class VersionMismatchDialog(
    private val currentVersion: Int,
    private val latestVersion: Int,
) : View() {

    init {
        title = "Version Check Result"
    }

    override val root = vbox(spacing = 10) {
        style {
            padding = box(10.px)
        }
        label("New version available") {
            addClass(Styles.header1)
        }
        label("You are using an out-of-date version: $currentVersion\nThe latest available version is: $latestVersion\n\nDownload it now from here:")
        link("https://github.com/christophpickl/allfit/releases")
        hbox {
            hgrow = Priority.ALWAYS
            alignment = Pos.CENTER
            button("Close") {
                action {
                    close()
                }
            }
        }
    }
}