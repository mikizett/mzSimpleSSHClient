package com.mz.sshclient.ssh;

import net.schmizz.sshj.common.KeyType;
import net.schmizz.sshj.common.SecurityUtils;
import net.schmizz.sshj.transport.verification.OpenSSHKnownHosts;

import java.io.File;
import java.io.IOException;
import java.security.PublicKey;

public class HostKeyVerifier extends OpenSSHKnownHosts {

    private final IHostKeyVerifyCallback callback;

    public HostKeyVerifier(final File knownHostFile, final IHostKeyVerifyCallback callback) throws IOException {
        super(knownHostFile);
        this.callback = callback;
    }

    @Override
    protected boolean hostKeyUnverifiableAction(String hostname, PublicKey key) {
        final KeyType keyType = KeyType.fromKey(key);

        if (callback != null && callback.getHostKeyUnverifiableConfirmation(hostname, key, keyType) == IHostKeyVerifyCallback.Confirmation.CONFIRMED) {
            try {
                this.entries.add(new HostEntry(null, hostname, KeyType.fromKey(key), key));
                write();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return true;
        }
        return false;
    }

    @Override
    protected boolean hostKeyChangedAction(String hostname, PublicKey key) {
        final KeyType type = KeyType.fromKey(key);
        final String fp = SecurityUtils.getFingerprint(key);
        final String path = getFile().getAbsolutePath();
        final String msg = String.format(
                "@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@\n"
                + "@    WARNING: REMOTE HOST IDENTIFICATION HAS CHANGED!     @\n"
                + "@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@\n"
                + "IT IS POSSIBLE THAT SOMEONE IS DOING SOMETHING NASTY!\n"
                + "Someone could be eavesdropping on you right now (man-in-the-middle attack)!\n"
                + "It is also possible that the host key has just been changed.\n"
                + "The fingerprint for the %s key sent by the remote host is\n" + "%s.\n"
                + "Do you still want to connect to this server?", type, fp, path);

        return callback == null || callback.getHostKeyChangedConfirmation(msg) == IHostKeyVerifyCallback.Confirmation.CONFIRMED;
    }

    @Override
    public boolean verify(String hostname, int port, PublicKey key) {
        try {
            if (!super.verify(hostname, port, key)) {
                return hostKeyUnverifiableAction(hostname, key);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return hostKeyUnverifiableAction(hostname, key);
        }
    }

    /**
     *
     */
    public interface IHostKeyVerifyCallback {

        enum Confirmation {
            CONFIRMED,
            NOT_CONFIRMED
        }

        Confirmation getHostKeyUnverifiableConfirmation(String hostname, PublicKey publicKey, KeyType type);
        Confirmation getHostKeyChangedConfirmation(String message);
    };

}
