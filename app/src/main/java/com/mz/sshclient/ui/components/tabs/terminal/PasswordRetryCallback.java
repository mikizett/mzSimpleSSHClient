package com.mz.sshclient.ui.components.tabs.terminal;

import com.mz.sshclient.model.SessionItemModel;
import com.mz.sshclient.mzSimpleSshClientMain;
import com.mz.sshclient.ssh.IPasswordRetryCallback;
import com.mz.sshclient.ssh.exceptions.SshOperationCanceledException;
import com.mz.sshclient.ui.utils.UIUtils;
import com.mz.sshclient.utils.Utils;

import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

public class PasswordRetryCallback implements IPasswordRetryCallback {

    private char[] cachedEncodedPassword;

    public PasswordRetryCallback(final SessionItemModel sessionItemModel) {
        if (sessionItemModel != null) {
            cachedEncodedPassword = sessionItemModel.getPassword().toCharArray();
        }
    }

    @Override
    public char[] getEncodedPassword(final String user) throws SshOperationCanceledException {
        if (cachedEncodedPassword != null) {
            return Utils.decodeCharArrayAsCharArray(cachedEncodedPassword);
        }

        final JTextField userTextField = new JTextField(30);
        userTextField.setText(user);

        final JPasswordField passwordField = new JPasswordField(30);
        UIUtils.addAncestorAndFocusListenerToPasswordField(passwordField);

        final JCheckBox cachePasswordCheckBox = new JCheckBox("Remember for this session");

        int answer = JOptionPane.showOptionDialog(
                mzSimpleSshClientMain.MAIN_FRAME,
                new Object[] {
                        "User", userTextField,
                        "Password", passwordField,
                        cachePasswordCheckBox
                },
                "Authentication",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null,
                null,
                null
        );
        if (answer == JOptionPane.OK_OPTION && cachePasswordCheckBox.isSelected()) {
            cachedEncodedPassword = Utils.encodeCharArrayAsCharArray(passwordField.getPassword());
            return Utils.decodeCharArrayAsCharArray(cachedEncodedPassword);
        } else {
            throw new SshOperationCanceledException("Canceled by user");
        }
    }
}
