package com.mz.sshclient.ui.components.tabs.terminal;

import com.mz.sshclient.model.SessionItemModel;
import com.mz.sshclient.mzSimpleSshClientMain;
import com.mz.sshclient.ssh.IPrivateKeyPasswordFinderCallback;
import com.mz.sshclient.ui.utils.UIUtils;
import com.mz.sshclient.utils.Utils;
import net.schmizz.sshj.userauth.password.Resource;

import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;

public class PrivateKeyPasswordFinderCallback implements IPrivateKeyPasswordFinderCallback {

    private boolean retry = true;

    private char[] cachedEncodedPassword;

    public PrivateKeyPasswordFinderCallback(final SessionItemModel sessionItemModel) {
        if (sessionItemModel != null) {
            cachedEncodedPassword = sessionItemModel.getPassword().toCharArray();
        }
    }

    @Override
    public char[] reqPassword(final Resource<?> resource) {
        if (cachedEncodedPassword != null) {
            retry = false;
            return Utils.decodeCharArrayAsCharArray(cachedEncodedPassword);
        }

        final JPasswordField passwordField = new JPasswordField();
        UIUtils.addAncestorAndFocusListenerToPasswordField(passwordField);

        final JCheckBox cachePasswordCheckBox = new JCheckBox("Remember for this session");

        int answer = JOptionPane.showOptionDialog(
                mzSimpleSshClientMain.MAIN_FRAME,
                new Object[] {
                        resource != null ? resource.toString() : "Private key passphrase:",
                        passwordField,
                        cachePasswordCheckBox
                },
                "Passphrase",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null,
                null,
                null
        );

        if (answer == JOptionPane.OK_OPTION && cachePasswordCheckBox.isSelected()) {
            cachedEncodedPassword = Utils.encodeCharArrayAsCharArray(passwordField.getPassword());
            return cachedEncodedPassword;
        }

        retry = false;

        return null;
    }

    @Override
    public boolean shouldRetry(final Resource<?> resource) {
        return retry;
    }

    @Override
    public char[] getEncodedCachedPassword() {
        return cachedEncodedPassword;
    }

}
