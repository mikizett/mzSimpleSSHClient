package com.mz.sshclient.ui.components.terminal;

import com.mz.sshclient.mzSimpleSshClientMain;
import com.mz.sshclient.ssh.IPasswordFinderCallback;
import com.mz.sshclient.ssh.IPasswordRetryCallback;
import com.mz.sshclient.ssh.exceptions.SshOperationCanceledException;

import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

public class PasswordRetryCallback implements IPasswordRetryCallback {

    @Override
    public char[] readPassword(String user, IPasswordFinderCallback passwordFinderCallback) throws SshOperationCanceledException {
        char[] password;

        final JTextField userTextField = new JTextField(30);
        final JPasswordField passwordField = new JPasswordField(30);
        final JCheckBox useCacheCheckBox = new JCheckBox("Remember for this session");
        userTextField.setText(user);
        passwordField.setFocusable(true);
        passwordField.setRequestFocusEnabled(true);
        passwordField.requestFocusInWindow();

        int answer = JOptionPane.showOptionDialog(
                mzSimpleSshClientMain.MAIN_FRAME,
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
        } else {
            throw new SshOperationCanceledException("Canceled by user");
        }

        return password;
    }
}
