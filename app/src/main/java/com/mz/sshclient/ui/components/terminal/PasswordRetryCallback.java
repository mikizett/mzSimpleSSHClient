package com.mz.sshclient.ui.components.terminal;

import com.mz.sshclient.ssh.IPasswordFinderCallback;
import com.mz.sshclient.ssh.IPasswordRetryCallback;

import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import java.awt.Window;

public class PasswordRetryCallback implements IPasswordRetryCallback {

    private final Window parentWindow;

    public PasswordRetryCallback(final Window parentWindow) {
        this.parentWindow = parentWindow;
    }

    @Override
    public char[] readPassword(String user, IPasswordFinderCallback passwordFinderCallback) {
        char[] password = null;

        final JTextField userTextField = new JTextField(30);
        final JPasswordField passwordField = new JPasswordField(30);
        final JCheckBox useCacheCheckBox = new JCheckBox("Remember for this session");
        userTextField.setText(user);
        int answer = JOptionPane.showOptionDialog(
                null,
                new Object[] {
                        "User", userTextField,
                        "Password", passwordField,
                        useCacheCheckBox
                },
                "Authentication",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null,
                null,
                null
        );
        if (answer == JOptionPane.OK_OPTION) {
            password = passwordField.getPassword();
            if (useCacheCheckBox.isSelected()) {
                passwordFinderCallback.cachePassword(password);
            }
        }

        return password;
    }
}
