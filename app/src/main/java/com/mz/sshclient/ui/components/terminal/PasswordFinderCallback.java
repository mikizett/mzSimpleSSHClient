package com.mz.sshclient.ui.components.terminal;

import com.mz.sshclient.ssh.IPasswordFinderCallback;
import net.schmizz.sshj.userauth.password.Resource;

import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import java.awt.Window;

public class PasswordFinderCallback implements IPasswordFinderCallback {

    private boolean retry = true;

    private char[] cachedPassword;
    private char[] cachedPassPhrase;

    private final Window parentWindow;

    public PasswordFinderCallback(final Window parentWindow) {
        this.parentWindow = parentWindow;
    }

    @Override
    public char[] getCachedPassword() {
        return cachedPassword;
    }

    @Override
    public void cachePassword(final char[] password) {
        this.cachedPassword = password;
    }

    @Override
    public char[] getCachedPassPhrase() {
        return cachedPassPhrase;
    }

    @Override
    public void setCachedPassPhrase(final char[] cachedPassPhrase) {
        this.cachedPassPhrase = cachedPassPhrase;
    }

    @Override
    public char[] reqPassword(final Resource<?> resource) {
        // if pass phrase was already cached
        if (cachedPassPhrase != null) {
            return cachedPassPhrase;
        }

        final JPasswordField txtPass = new JPasswordField();
        final JCheckBox chkUseCache = new JCheckBox("Remember for this session");

        int answer = JOptionPane.showOptionDialog(
                parentWindow,
                new Object[] {
                        resource != null ? resource.toString() : "Private key passphrase:",
                        txtPass,
                        chkUseCache
                },
                "Passphrase",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null,
                null,
                null
        );

        if (answer == JOptionPane.OK_OPTION) {
            final char[] passPhrase = txtPass.getPassword();
            if (chkUseCache.isSelected()) {
                this.cachedPassPhrase = passPhrase;
            }
            return passPhrase;
        }
        retry = false;

        return null;
    }

    @Override
    public boolean shouldRetry(final Resource<?> resource) {
        return retry;
    }
}
