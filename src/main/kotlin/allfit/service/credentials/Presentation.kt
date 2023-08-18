package allfit.service.credentials

import allfit.api.Credentials
import java.awt.BorderLayout
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.imageio.ImageIO
import javax.swing.BorderFactory
import javax.swing.ImageIcon
import javax.swing.JButton
import javax.swing.JDialog
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTextField

fun interface CredentialsInitDialogCallback {
    fun done(credentials: Credentials?)
}

sealed interface CredentialsMode {
    val infoText: String

    fun setEmailTextOn(txtEmail: JTextField)

    data object InitialSet : CredentialsMode {
        override val infoText = "Enter your initial OneFit login credentials"

        override fun setEmailTextOn(txtEmail: JTextField) {
            // do nothing
        }
    }

    class UpdateOnError(private val defaultEmail: String) : CredentialsMode {
        override val infoText = "Authentication failed. Please update your credentials."
        override fun setEmailTextOn(txtEmail: JTextField) {
            txtEmail.text = defaultEmail
        }
    }
}

class CredentialsInitDialog(
    private val mode: CredentialsMode,
    private val callback: CredentialsInitDialogCallback,
) {

    private val imagePath = "/images/logo.png"
    private val dialog = JDialog(null as JFrame?, "OneFit Authentication", true)
    private val txtEmail = JTextField(30)
    private val txtPassword = JTextField(30)
    private val saveButton = JButton(
        when (mode) {
            is CredentialsMode.InitialSet -> "Save"
            is CredentialsMode.UpdateOnError -> "Save and Exit"
        }
    )

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            CredentialsInitDialog(
                CredentialsMode.InitialSet
//                CredentialsMode.Update("foo@bar.com")
            ) {}.show()
        }
    }

    init {
        mode.setEmailTextOn(txtEmail)

        dialog.contentPane = buildRootPanel()
        dialog.rootPane.defaultButton = saveButton
        dialog.pack()
        dialog.isResizable = false
        dialog.setLocationRelativeTo(null)

        dialog.defaultCloseOperation = JDialog.DO_NOTHING_ON_CLOSE
        dialog.addWindowListener(object : WindowAdapter() {
            override fun windowClosing(e: WindowEvent) {
                finish(isAborted = true)
            }
        })
    }

    fun show() {
        dialog.isVisible = true
    }

    private fun buildRootPanel(): JPanel {
        val buttons = JPanel()
        saveButton.addActionListener {
            finish(isAborted = false)
        }
        buttons.add(saveButton)

        val rootPanel = JPanel(BorderLayout())
        rootPanel.border = BorderFactory.createEmptyBorder(5, 5, 5, 5)
        rootPanel.add(buildInfoPanel(), BorderLayout.NORTH)
        rootPanel.add(buildInputsPanel(), BorderLayout.CENTER)
        rootPanel.add(buttons, BorderLayout.SOUTH)
        return rootPanel
    }

    private fun buildInfoPanel(): JPanel {
        val imageStream = CredentialsInitDialog::class.java.getResourceAsStream(imagePath) ?: error("Image not found!")
        val image = JLabel(ImageIcon(ImageIO.read(imageStream)))

        val panel = JPanel(BorderLayout())
        panel.add(image, BorderLayout.CENTER)
        panel.add(JLabel(mode.infoText), BorderLayout.SOUTH)
        return panel
    }

    private fun buildInputsPanel(): JPanel {
        val c = GridBagConstraints()
        val inputsLayout = GridBagLayout()
        val inputs = JPanel(inputsLayout)
        inputsLayout.setConstraints(inputs, c)
        c.anchor = GridBagConstraints.WEST

        c.gridx = 0
        c.gridy = 0
        c.insets = Insets(0, 0, 2, 5)
        inputs.add(JLabel("Email:"), c)

        c.gridx++
        c.insets = Insets(0, 0, 2, 0)
        inputs.add(txtEmail, c)

        c.gridy++
        c.gridx = 0
        c.insets = Insets(0, 0, 2, 5)
        inputs.add(JLabel("Password:"), c)

        c.gridx++
        c.insets = Insets(0, 0, 2, 0)
        inputs.add(txtPassword, c)

        return inputs
    }

    private fun finish(isAborted: Boolean) {
        dialog.isVisible = false
        callback.done(
            if (isAborted) null else {
                Credentials(
                    email = txtEmail.text,
                    clearTextPassword = txtPassword.text,
                )
            }
        )
    }

}
