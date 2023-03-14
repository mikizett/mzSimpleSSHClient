package com.mz.sshclient.ui.components.terminal;

import com.mz.sshclient.ssh.HostKeyVerifier;
import com.mz.sshclient.ui.utils.MessageDisplayUtil;
import net.schmizz.sshj.common.KeyType;
import net.schmizz.sshj.common.SecurityUtils;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.awt.Window;
import java.security.PublicKey;
import java.util.concurrent.atomic.AtomicInteger;

public class HostKeyVerificationCallback implements HostKeyVerifier.IHostKeyVerifyCallback {

    private final Window parentWindow;

    public HostKeyVerificationCallback(final Window parentWindow) {
        this.parentWindow = parentWindow;
    }

    @Override
    public Confirmation getHostKeyUnverifiableConfirmation(final String hostname, final PublicKey publicKey, final KeyType keyType) {
        final String msg = String.format(
                "The authenticity of host '%s' can't be established.\n%s key fingerprint is %s.\\nAre you sure you want to continue connecting (yes/no)?",
                hostname, keyType, SecurityUtils.getFingerprint(publicKey)
        );

        int answer = MessageDisplayUtil.showYesNoConfirmDialog(parentWindow, msg, "Question");
        return answer == JOptionPane.YES_OPTION ? Confirmation.CONFIRMED : Confirmation.NOT_CONFIRMED;
    }

    @Override
    public Confirmation getHostKeyChangedConfirmation(final String message) {
        int answer = MessageDisplayUtil.showYesNoConfirmDialog(parentWindow, message, "Question");
        return answer == JOptionPane.YES_OPTION ? Confirmation.CONFIRMED : Confirmation.NOT_CONFIRMED;
    }

}
