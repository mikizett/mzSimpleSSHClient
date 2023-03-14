package com.mz.sshclient.ssh.exceptions;

public class SshPrivateKeyMissingException extends Exception {

    public SshPrivateKeyMissingException(final String message) {
        super(message);
    }

    public SshPrivateKeyMissingException(final String message, final Throwable cause) {
        super(message, cause);
    }

}
