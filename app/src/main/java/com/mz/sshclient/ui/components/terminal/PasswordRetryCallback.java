package com.mz.sshclient.ui.components.terminal;

import com.mz.sshclient.mzSimpleSshClientMain;
import com.mz.sshclient.ssh.IPasswordFinderCallback;
import com.mz.sshclient.ssh.IPasswordRetryCallback;
import com.mz.sshclient.ssh.exceptions.SshOperationCanceledException;
import com.mz.sshclient.ui.utils.UIUtils;

import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

public class PasswordRetryCallback implements IPasswordRetryCallback {

    @Override
    public char[] readPassword(String user, IPasswordFinderCallback passwordFinderCallback) throws SshOperationCanceledException {
        char[] password;

        final JTextField userTextField = new JTextField(30);
        userTextField.setText(user);

        final JPasswordField passwordField = new JPasswordField(30);
        UIUtils.addAncestorAndFocusListenerToPasswordField(passwordField);

        final JCheckBox useCacheCheckBox = new JCheckBox("Remember for this session");

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
