package allfit.service.credentials

import allfit.api.Credentials
import java.awt.BorderLayout
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.JButton
import javax.swing.JDialog
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTextField

fun interface CredentialsInitDialogCallback {
    fun done(credentials: Credentials?)
}

class CredentialsInitDialog(private val callback: CredentialsInitDialogCallback) {

    private val dialog = JDialog(null as JFrame?, "OneFit Credentials", true)
    private val txtEmail = JTextField(30)
    private val txtPassword = JTextField(30)
    private val saveButton = JButton("Save")

    init {
        dialog.contentPane = buildRootPanel()
        dialog.pack()
        dialog.isResizable = false
        dialog.setLocationRelativeTo(null)
        dialog.defaultCloseOperation = JDialog.DO_NOTHING_ON_CLOSE
        dialog.rootPane.defaultButton = saveButton
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
        rootPanel.add(buildInputsPanel(), BorderLayout.CENTER)
        rootPanel.add(buttons, BorderLayout.SOUTH)
        return rootPanel
    }

    private fun buildInputsPanel(): JPanel {
        val c = GridBagConstraints()
        val inputsLayout = GridBagLayout()
        val inputs = JPanel(inputsLayout)
        inputsLayout.setConstraints(inputs, c)
        c.anchor = GridBagConstraints.WEST
        c.gridx = 0
        c.gridy = 0
        inputs.add(JLabel("Email:"), c)
        c.gridx++
        inputs.add(txtEmail, c)
        c.gridy++
        c.gridx = 0
        inputs.add(JLabel("Password:"), c)
        c.gridx++
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
