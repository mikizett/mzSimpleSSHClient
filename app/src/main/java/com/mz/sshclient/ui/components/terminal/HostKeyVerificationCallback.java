package com.mz.sshclient.ui.components.terminal;

import com.mz.sshclient.mzSimpleSshClientMain;
import com.mz.sshclient.ssh.HostKeyVerifier;
import com.mz.sshclient.ui.utils.MessageDisplayUtil;
import net.schmizz.sshj.common.KeyType;
import net.schmizz.sshj.common.SecurityUtils;

import javax.swing.JOptionPane;
import java.security.PublicKey;

public class HostKeyVerificationCallback implements HostKeyVerifier.IHostKeyVerifyCallback {

    @Override
    public Confirmation getHostKeyUnverifiableConfirmation(final String hostname, final PublicKey publicKey, final KeyType keyType) {
        final String msg = String.format(
                "The authenticity of host '%s' can't be established.\n%s key fingerprint is %s.\nAre you sure you want to continue connecting (yes/no)?",
                hostname, keyType, SecurityUtils.getFingerprint(publicKey)
        );

        int answer = MessageDisplayUtil.showYesNoConfirmDialog(mzSimpleSshClientMain.MAIN_FRAME, msg, "Question");
        return answer == JOptionPane.YES_OPTION ? Confirmation.CONFIRMED : Confirmation.NOT_CONFIRMED;
    }

    @Override
    public Confirmation getHostKeyChangedConfirmation(final String message) {
        int answer = MessageDisplayUtil.showYesNoConfirmDialog(mzSimpleSshClientMain.MAIN_FRAME, message, "Question");
        return answer == JOptionPane.YES_OPTION ? Confirmation.CONFIRMED : Confirmation.NOT_CONFIRMED;
    }

}
